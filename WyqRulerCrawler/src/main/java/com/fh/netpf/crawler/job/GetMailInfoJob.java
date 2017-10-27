package com.fh.netpf.crawler.job;

import com.fh.netpf.crawler.conf.SystemConfigure;
import com.fh.netpf.crawler.thread.GetMailInfoThread;
import lombok.extern.log4j.Log4j2;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.mail.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Log4j2
public class GetMailInfoJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        log.info("接收邮件中...");

        int mailSize = Integer.parseInt(SystemConfigure.getInstance().getProperty("mailSize"));
        String protocol = SystemConfigure.getInstance().getProperty("protocol");
        String isSSL = SystemConfigure.getInstance().getProperty("isSSL");
        String host = SystemConfigure.getInstance().getProperty("host");
        String port = SystemConfigure.getInstance().getProperty("port");
        String username = SystemConfigure.getInstance().getProperty("username");
        String password = SystemConfigure.getInstance().getProperty("password");

        Properties properties = new Properties();
        properties.put("mail.pop3.ssl.enable", isSSL);
        properties.put("mail.pop3.host", host);
        properties.put("mail.pop3.port", port);
        Session session = Session.getDefaultInstance(properties);

        Store store = null;
        Folder folder = null;
        try {
            store = session.getStore(protocol);
            store.connect(host, username, password);

            folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);

            int count = folder.getMessageCount();
            int size = count - mailSize;
            if (size > 0) {
                List<Message> messages = new ArrayList<>();
                Message[] message = folder.getMessages();
                for (int i = mailSize; i < count; i ++ ) {
                    messages.add(message[i]);
                }
                Thread thread = new Thread(new GetMailInfoThread(messages));
                thread.start();
            }

            SystemConfigure.getInstance().setProperty("mailSize", count);

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            log.error("{}", e);
        } catch (MessagingException e) {
            e.printStackTrace();
            log.error("{}", e);
        }
    }
}
