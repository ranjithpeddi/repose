package com.rackspace.papi.service.httpclient.impl;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread runner which will  monitor and shutdown when no active connections are being processed by passed
 * HttpClient(s)
 */
public class ClientDecommissioner implements Runnable {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ClientDecommissioner.class);

    private static final long DEFAULT_INTERVAL = 5000;
    public static final String UNIQUE_ID = "UNIQUE_ID";
    List<HttpClient> clientList;
    private boolean done;
    private Object listLock;
    HttpClientUserManager userManager;

    public ClientDecommissioner(HttpClientUserManager userManager) {

        clientList = new ArrayList<HttpClient>();
        listLock = new Object();
        done = false;
        this.userManager = userManager;
    }

    public void addClientToBeDecommissioned(HttpClient client) {

        synchronized (listLock) {
            PoolingClientConnectionManager connMan = (PoolingClientConnectionManager) client.getConnectionManager();
            connMan.closeExpiredConnections();
            connMan.setMaxTotal(1);
            connMan.setDefaultMaxPerRoute(1);
            clientList.add(client);
        }
    }

    /*TODO: yagni*/
    public void addClientsToBeDecommissioned(List<HttpClient> clients) {
        for (HttpClient client : clients) {
            addClientToBeDecommissioned(client);
        }
    }

    public void stop() {
        this.done = true;
    }

    @Override
    public void run() {
        while (!this.done) {
            synchronized (listLock) {

                LOG.trace("Iterating through decommissioned clients...");

                List<HttpClient> clientsToRemove = new ArrayList<HttpClient>();

                for (HttpClient client : clientList) {

                    String clientId = client.getParams().getParameter(UNIQUE_ID).toString();

                    if (userManager.hasUsers(clientId)) {
                        LOG.warn("Failed to shutdown client {} due to users still registered.", clientId);
                        break;
                    }

                    PoolingClientConnectionManager connMan = (PoolingClientConnectionManager) client.getConnectionManager();
                    PoolStats stats = connMan.getTotalStats();

                    if (stats.getLeased() == 0) {   // if no active connections we will shutdown this client
                        LOG.debug("Shutting down client: " + client.hashCode());
                        connMan.shutdown();
                        clientsToRemove.add(client);
                    }
                }
                for(HttpClient client: clientsToRemove) {
                    clientList.remove(client);
                    LOG.info("HTTP connection pool {} has been destroyed.", client.getParams().getParameter(UNIQUE_ID));
                }
            }

            try {
                Thread.sleep(DEFAULT_INTERVAL);
            } catch (InterruptedException ex) {
               break;
            }

        }

        LOG.error("Shutting down decommissioner");
        Thread.currentThread().interrupt();
    }
}
