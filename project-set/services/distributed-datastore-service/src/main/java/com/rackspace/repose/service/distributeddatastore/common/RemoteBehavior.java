package com.rackspace.repose.service.distributeddatastore.common;

/**
 *
 * @author zinic
 */
public enum RemoteBehavior {

   ALLOW_FORWARDING,
   DISALLOW_FORWARDING;

   public static RemoteBehavior valueOfOrNull(String enumName) {
      final String uppercaseEnumName = enumName.toUpperCase();

      for (RemoteBehavior behavior : values()) {
         if (behavior.name().equals(uppercaseEnumName)) {
            return behavior;
         }
      }

      return null;
   }
}
