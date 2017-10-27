package com.fh.netpf.crawler.conf;

import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class SystemConfigure {

    private static SystemConfigure INSTANCE = null;

    private static final String CONFIG_FILE = "src/main/resources/crawler.properties";
    // private static final String CONFIG_FILE = "crawler.properties";

    private static Map<String, String> KV = new ConcurrentHashMap<>(64);

    private SystemConfigure() {
        try {
            load();
        } catch (Exception e) {
            log.error("{}", e);
        }
    }

    public synchronized static SystemConfigure getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new SystemConfigure();
        }
        return INSTANCE;
    }

    public String getProperty(String key) {
        String value = null;
        if (null != KV && null != key && key.trim().length() > 0) {
            value = KV.get(key.trim());
        }
        return value;
    }

    public void setProperty(String key, Object value) {
        if (null == key || key.trim().length() == 0) {
            return;
        }
        if (value instanceof Integer) {
            KV.put(key.trim(), ((Integer) value).toString());
        } else if (value instanceof Long) {
            KV.put(key.trim(), ((Long) value).toString());
        } else if (value instanceof String) {
            KV.put(key.trim(), ((String) value));
        }
        write();
    }

    private void load(){

        InputStream inputStream = null;
        try {

            inputStream = new FileInputStream(new File(CONFIG_FILE));
            Properties properties = new Properties();
            properties.load(inputStream);

            if (properties != null && properties.size() > 0) {
                KV.clear();
                Iterator<Object> i = properties.keySet().iterator();
                while (i.hasNext()) {
                    Object key = i.next();
                    KV.put((String) key, (String) properties.get(key));
                    log.info("Load property [" + key + "] values [" + properties.get(key) + "]!");
                }
            }
        } catch (IOException e) {
            log.error("{}", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("{}", e);
                }
            }
        }
    }

    private void write() {

        if (null == KV || KV.size() == 0) {
            return;
        }
        Properties properties = new Properties();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(CONFIG_FILE);

            Iterator<String> i = KV.keySet().iterator();
            while (i.hasNext()) {
                String key = i.next();
                String value = KV.get(key);
                properties.setProperty(key, value);
            }
            properties.store(fos, "SAVE");
        } catch (FileNotFoundException e) {
            log.error("{}", e);
        } catch (IOException e) {
            log.error("{}", e);
        } catch (Exception e) {
            log.error("{}", e);
        } finally {
            try {
                if (null != fos) {
                    fos.close();
                }
            } catch (IOException e) {
                log.error("{}", e);
            }
        }
    }

}
