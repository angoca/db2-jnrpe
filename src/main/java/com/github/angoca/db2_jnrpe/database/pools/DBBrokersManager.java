package com.github.angoca.db2_jnrpe.database.pools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class DBBrokersManager {
    private static DBBrokersManager instance;

    public static DBBrokersManager getInstance() {
        if (instance == null) {
            instance = new DBBrokersManager();
        }
        return instance;
    }

    final private Map<String, DBBroker> brokers;

    private DBBrokersManager() {
        this.brokers = new HashMap<String, DBBroker>();
    }

    public DBBroker getBroker(final String brokerName) {
        DBBroker broker = this.brokers.get(brokerName);
        if (broker == null) {
            final Class<?> clazz;
            try {
                clazz = Class.forName(brokerName);
                Method method = clazz.getMethod("getInstance");
                broker = (DBBroker) method.invoke(null);
                this.brokers.put(brokerName, broker);
            } catch (final ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return broker;
    }
}
