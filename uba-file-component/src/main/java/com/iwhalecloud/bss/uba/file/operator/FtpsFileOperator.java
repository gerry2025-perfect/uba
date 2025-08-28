package com.iwhalecloud.bss.uba.file.operator;

import com.iwhalecloud.bss.uba.common.releaser.AutoReleaser;
import com.iwhalecloud.bss.uba.file.magic.resource.FileInfo;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.ftp.FTPReply;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

/**
 * FTPS文件操作实现类
 */
public class FtpsFileOperator extends FtpFileOperator {

    public FtpsFileOperator(FileInfo fileInfo) {
        super(fileInfo);
        // 测试环境跳过证书验证
        if (!fileInfo.isProduction()) {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }}, null);
                ftpClient = new FTPSClient(sslContext);
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }else {
            this.ftpClient = new FTPSClient("TLS", true);
        }
    }

    /**
     * 连接FTPS服务器
     */
    protected void connect() {
        try {
            AutoReleaser.updateLastOperateTime(this, false);
            if (ftpClient.isConnected()) {
                return;
            }

            // 连接服务器
            int port = fileInfo.getPort() > 0 ? fileInfo.getPort() : 21;
            ftpClient.connect(fileInfo.getHost(), port);
            // 检查连接响应
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftpClient.disconnect();
                throw new RuntimeException("FTPS服务器连接失败，响应码: " + replyCode);
            }
            // 升级到加密连接 (显式FTPS)
            ((FTPSClient)ftpClient).execAUTH("TLS");
            // 登录
            if (!ftpClient.login(fileInfo.getUsername(), fileInfo.getPassword())) {
                ftpClient.disconnect();
                throw new RuntimeException("FTPS登录失败");
            }

            // 设置传输模式为二进制
            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            // 使用被动模式
            ftpClient.enterLocalPassiveMode();

        } catch (Exception e) {
            throw new RuntimeException("FTPS连接异常", e);
        }
    }

}
