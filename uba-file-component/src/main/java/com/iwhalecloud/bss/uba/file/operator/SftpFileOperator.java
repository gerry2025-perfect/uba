package com.iwhalecloud.bss.uba.file.operator;

import com.iwhalecloud.bss.uba.common.exception.ExceptionDefine;
import com.iwhalecloud.bss.uba.common.exception.UbaException;
import com.iwhalecloud.bss.uba.common.releaser.AutoReleaser;
import com.iwhalecloud.bss.uba.common.releaser.IReleasable;
import com.iwhalecloud.bss.uba.file.magic.resource.FileInfo;
import com.jcraft.jsch.*;
import com.ztesoft.zsmart.core.log.ZSmartLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * SFTP文件操作实现类
 */
public class SftpFileOperator implements IFileOperator, IReleasable {

    private static final ZSmartLogger logger = ZSmartLogger.getLogger(SftpFileOperator.class);

    private final FileInfo fileInfo;
    private ChannelSftp sftpChannel;
    private Session session;

    public SftpFileOperator(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    /**
     * 连接SFTP服务器
     */
    private void connect() {
        try {
            AutoReleaser.updateLastOperateTime(this, false);
            if (sftpChannel != null && sftpChannel.isConnected()) {
                return;
            }

            logger.debug(String.format("open sftp session, key: %s, host: %s, port: %d, username: %s",
                    fileInfo.getKey(), fileInfo.getHost(), fileInfo.getPort(), fileInfo.getUsername()));
            JSch jsch = getJSch();
            // 创建会话
            int port = fileInfo.getPort() > 0 ? fileInfo.getPort() : 22;
            session = jsch.getSession(fileInfo.getUsername(), fileInfo.getHost(), port);

            // 设置密码（如果没有使用密钥认证）
            if (fileInfo.getPrivateKeyPath() == null && fileInfo.getPassword() != null) {
                session.setPassword(fileInfo.getPassword());
            }

            // 配置会话
            java.util.Properties config = new java.util.Properties();
            // 测试环境不严格检查主机密钥
            if (fileInfo.getKnownHostsPath()==null && fileInfo.getKnownHostsContent()==null) {
                config.put("StrictHostKeyChecking", "no");
            } else {
                config.put("StrictHostKeyChecking", "yes");
            }
            session.setConfig(config);

            // 连接超时时间：30秒
            session.connect(fileInfo.getTimeout());
            // 打开SFTP通道
            Channel channel = session.openChannel("sftp");
            channel.connect(fileInfo.getTimeout()); // 通道连接超时时间：30秒
            sftpChannel = (ChannelSftp) channel;
            logger.debug("connect to sftp success");
        } catch (JSchException e) {
            throw new RuntimeException("connect to sftp fail ", e);
        }
    }

    private JSch getJSch() throws JSchException {
        JSch jsch = new JSch();

        // 配置已知主机（生产环境）
        if (fileInfo.isProduction()) {
            if (fileInfo.getKnownHostsPath() != null) {
                jsch.setKnownHosts(fileInfo.getKnownHostsPath());
            } else if (fileInfo.getKnownHostsContent() != null) {
                // 支持已知主机内容字符串
                jsch.setKnownHosts(new ByteArrayInputStream(fileInfo.getKnownHostsContent().getBytes()));
            }
        }

        // 处理认证方式：优先使用私钥字符串，其次是私钥文件，最后是密码
        if (fileInfo.getPrivateKeyContent() != null) {
            // 1. 使用私钥字符串认证
            addPrivateKeyFromString(jsch, fileInfo.getPrivateKeyContent(), fileInfo.getPassphrase());
        } else if (fileInfo.getPrivateKeyPath() != null) {
            // 2. 使用私钥文件认证
            if (fileInfo.getPassphrase() != null) {
                jsch.addIdentity(fileInfo.getPrivateKeyPath(), fileInfo.getPassphrase());
            } else {
                jsch.addIdentity(fileInfo.getPrivateKeyPath());
            }
        }
        return jsch;
    }

    /**
     * 从字符串加载私钥
     */
    private void addPrivateKeyFromString(JSch jsch, String privateKeyContent, String passphrase) throws JSchException {
        // 私钥字符串可能没有换行符，需要格式化（PEM格式要求）
        String formattedKey = formatPrivateKey(privateKeyContent);

        // 将字符串转换为输入流
        if (passphrase != null && !passphrase.isEmpty()) {
            jsch.addIdentity(null, formattedKey.getBytes(), null, passphrase.getBytes());
        } else {
            jsch.addIdentity(null, formattedKey.getBytes(), null, null);
        }
    }

    /**
     * 格式化私钥字符串为标准PEM格式
     * PEM格式要求：以-----BEGIN...-----开头，-----END...-----结尾，中间内容有换行
     */
    private String formatPrivateKey(String privateKey) {
        if (privateKey == null) {
            return null;
        }

        // 移除所有换行符，便于重新格式化
        String key = privateKey.replaceAll("\\r|\\n", "");

        // 匹配私钥头部
        String beginMarker = "-----BEGIN";
        String endMarker = "-----END";

        int beginIndex = key.indexOf(beginMarker);
        int endIndex = key.indexOf(endMarker);

        if (beginIndex == -1 || endIndex == -1 || endIndex <= beginIndex) {
            return privateKey; // 格式不符合预期，返回原始字符串
        }

        // 提取头部和内容
        String header = key.substring(beginIndex, key.indexOf("-----", beginIndex) + 5);
        String content = key.substring(header.length(), endIndex);
        String footer = key.substring(endIndex);

        // 内容部分每64个字符添加一个换行符（PEM格式规范）
        StringBuilder formattedContent = new StringBuilder();
        for (int i = 0; i < content.length(); i += 64) {
            int end = Math.min(i + 64, content.length());
            formattedContent.append(content.substring(i, end)).append("\n");
        }

        // 组合成完整的PEM格式
        return header + "\n" + formattedContent + footer + "\n";
    }

    public void disconnect() {
        release(false);
    }

    /**
     * 断开SFTP连接
     */
    public void release(boolean isForce) {
        if(!isForce) {
            AutoReleaser.updateLastOperateTime(this, true);
        }
        if (sftpChannel != null && sftpChannel.isConnected()) {
            sftpChannel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        logger.debug(String.format("disconnect success, key: %s", fileInfo.getKey()));
    }

    @Override
    public InputStream getFile(String filePath) {
        try {
            connect();
            return sftpChannel.get(buildFullPath(fileInfo.getRootDir(), filePath));
        } catch (SftpException e) {
            throw new RuntimeException("obtaining the file stream error: " + filePath, e);
        }
    }

    @Override
    public void removeTo(String sourceFilePath, String targetFilePath) {
        try {
            connect();
            String source = buildFullPath(fileInfo.getRootDir(), sourceFilePath);
            String target = buildFullPath(fileInfo.getRootDir(), targetFilePath);

            sftpChannel.rename(source, target);
        } catch (SftpException e) {
            throw new RuntimeException("removing sftp file error: " + sourceFilePath + " -> " + targetFilePath, e);
        }
    }

    @Override
    public List<String> getFileNames(String fileDir) {
        try {
            connect();
            String dir = buildFullPath(fileInfo.getRootDir(), fileDir);

            Vector<ChannelSftp.LsEntry> entries = sftpChannel.ls(dir);
            List<String> fileNames = new ArrayList<>();

            for (ChannelSftp.LsEntry entry : entries) {
                // 跳过当前目录(.)和父目录(..)
                if (!".".equals(entry.getFilename()) && !"..".equals(entry.getFilename())) {
                    fileNames.add(entry.getFilename());
                }
            }

            return fileNames;
        } catch (SftpException e) {
            throw new RuntimeException("fetching file name list: " + fileDir, e);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            connect();
            sftpChannel.rm(buildFullPath(fileInfo.getRootDir(), filePath));
        } catch (SftpException e) {
            throw new RuntimeException("delete sftp file error: " + filePath, e);
        }
    }

    @Override
    public void writeFile(byte[] data, String filePath) {
        try (InputStream is = new ByteArrayInputStream(data)) {
            connect();
            String fullPath = buildFullPath(fileInfo.getRootDir(), filePath);

            // 创建父目录
            String parentDir = fullPath.substring(0, fullPath.lastIndexOf('/'));
            createDir(parentDir);

            sftpChannel.put(is, fullPath);
        } catch (IOException | SftpException e) {
            throw new RuntimeException("update sftp file error: " + filePath, e);
        }
    }

    @Override
    public void createDir(String dirName) {
        try {
            connect();
            String fullDir = buildFullPath(fileInfo.getRootDir(), dirName);

            // 处理多级目录
            String[] dirs = fullDir.split("/");
            StringBuilder currentDir = new StringBuilder();

            for (String dir : dirs) {
                if (dir.isEmpty()) continue;

                currentDir.append("/").append(dir);
                String dirPath = currentDir.toString();

                try {
                    sftpChannel.lstat(dirPath);
                } catch (SftpException e) {
                    if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                        sftpChannel.mkdir(dirPath);
                    } else {
                        throw e;
                    }
                }
            }
        } catch (SftpException e) {
            throw new RuntimeException("creat sftp folder error: " + dirName, e);
        }
    }

    /**校验远程文件是否存在*/
    public boolean exists(String filePath, boolean throwException) {
        connect();
        // 检查远程文件是否存在
        try {
            sftpChannel.lstat(filePath);
            return true;
        } catch (SftpException e) {
            if (throwException) {
                throw new UbaException(ExceptionDefine.COMM_ERROR_TYPE_FILE_NOT_EXISTS,String.format(", remote file path: %s", filePath),null);
            }
            return false;
        }
    }

    public boolean removeSelf() {
        return true;
    }

    public static void main(String[] args) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setRootDir("/");
        fileInfo.setKey("localhost");
        fileInfo.setUsername("llin");
        fileInfo.setPassword("llin");
        fileInfo.setHost("localhost");
        SftpFileOperator sftpFileOperator = new SftpFileOperator(fileInfo);
        try {
            System.out.println(sftpFileOperator.getFileNames(""));
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            sftpFileOperator.release(false);
        }

    }

}
