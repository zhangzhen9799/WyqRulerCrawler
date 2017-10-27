package com.fh.netpf.crawler.thread;

import com.fh.netpf.crawler.email.MailInfo;
import com.fh.netpf.crawler.main.AppContext;
import com.fh.netpf.crawler.task.Task;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.regex.Matcher;
import static java.util.regex.Pattern.*;

/**
 * 获取邮箱中邮件信息的线程
 */
@Log4j2
@AllArgsConstructor
public class GetMailInfoThread implements Runnable {

    private List<Message> messages;

    @Override
    public void run() {
        if (messages.size() == 0) {
            return;
        }
        for (int i = 0; i < messages.size(); i ++) {
            try {
                MailInfo mailInfo = new MailInfo((MimeMessage) messages.get(i));
                log.info("----------第" + (i + 1) + "封邮件----------");
                log.info("邮件主题:{}", mailInfo.getSubject());
                log.info("邮件是否需要回复:{}", mailInfo.getReplySign());
                log.info("邮件是否已读:{}", mailInfo.isNew());
                log.info("邮件是否包含附件:{}", mailInfo.isContainAttach((Part) messages.get(i)));
                log.info("邮件发送时间:{}", mailInfo.getSentDate());
                log.info("邮件发送人地址:{}", mailInfo.getFrom());
                log.info("邮件收信人地址:{}", mailInfo.getMailAddress("TO"));
                log.info("邮件抄送:{}", mailInfo.getMailAddress("CC"));
                log.info("邮件密送:{}", mailInfo.getMailAddress("BCC"));
                log.info("邮件发送时间:{}", mailInfo.getSentDate());
                log.info("邮件ID:{}", mailInfo.getMessageId());
                log.info("邮件正文内容:{}", mailInfo.getBodyText((Part) messages.get(i)));
                Document document = Jsoup.parse(mailInfo.getBodyText((Part) messages.get(i)));
                String href = "";
                String pageSize = "10";
                Elements elements = document.select("tr.bg").select("span.c_2");
                if (elements.size() != 0) {
                    pageSize = elements.first().text();
                }
                log.info("PageSize:{}", pageSize);
                href = document.select("a.sel_btn").attr("href");
                log.info("href:{}", href);
                if (!"".equals(href)) {
                    Matcher matcher = compile("reviewCode=([0-9A-Za-z]+)").matcher(href);
                    if (matcher.find()) {
                        String reviewCode = matcher.group(1);
                        log.info("ReviewCode:{}", reviewCode);
                        Task task = new Task();
                        task.setPageSize(pageSize);
                        task.setReviewCode(reviewCode);
                        AppContext.blockingQueueFile.put(task);
                    }
                }
            } catch (MessagingException e) {
                log.error("{}", e);
            } catch (Exception e) {
                log.error("{}", e);
            }
        }
    }
}