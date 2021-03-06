/*
 * _=_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=
 * Repose
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Copyright (C) 2010 - 2015 Rackspace US, Inc.
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=_
 */
package org.openrepose.commons.utils.http.header;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zinic
 */
public class HeaderValueParser {

    private final String rawHeaderValue;

    public HeaderValueParser(String rawValue) {
        this.rawHeaderValue = rawValue;
    }

    public HeaderValue parse() throws MalformedHeaderValueException {
        final Map<String, String> parameters = new HashMap<String, String>();
        final String[] parameterSplit = rawHeaderValue.split(";");
        final StringBuilder value = new StringBuilder(parameterSplit[0]);

        if (parameterSplit.length > 1) {
            // Start at index 1 since that's the actual header value
            for (int i = 1; i < parameterSplit.length; i++) {
                String param = parameterSplit[i];
                if (param.contains("=")) {
                    parseParameter(parameters, parameterSplit[i]);

                } else {
                    value.append(";").append(param);
                }
            }
        }

        return new HeaderValueImpl(value.toString().trim(), parameters);
    }

    private String concat(String[] values, int start, String delimiter) {
        StringBuilder sb = new StringBuilder("");
        int index = start;

        while (index < values.length) {
            if (sb.length() > 0) {
                sb.append(delimiter);
            }

            sb.append(values[index++].trim());
        }

        return sb.toString();
    }

    private void parseParameter(Map<String, String> parameters, String unparsedParameter) throws MalformedHeaderValueException {
        final String[] keyValueSplit = unparsedParameter.split("=");

        // For a possible parameter to be valid it must have the '=' character
        switch (keyValueSplit.length) {
            case 2:
                parameters.put(keyValueSplit[0].trim(), keyValueSplit[1].trim());
                break;
            case 0:
                throw new MalformedHeaderValueException("Valid parameter expected for header. Got: " + unparsedParameter);
            default:
                parameters.put(keyValueSplit[0].trim(), concat(keyValueSplit, 1, "="));
                break;
        }
    }
}
