package com.iwhalecloud.bss.uba.file.operator;

import com.iwhalecloud.bss.uba.common.releaser.AutoReleaser;
import com.iwhalecloud.bss.uba.common.releaser.IReleasable;
import com.iwhalecloud.bss.uba.file.magic.resource.FileInfo;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * FTP文件操作实现类
 */
public class FtpFileOperator implements IFileOperator, IReleasable {
    protected final FileInfo fileInfo;
    protected FTPClient ftpClient;

    public FtpFileOperator(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
        this.ftpClient = new FTPClient();
    }

    /**
     * 连接FTP服务器
     */
    protected void connect() {
        try {
            AutoReleaser.updateLastOperateTime(this, false);
            if (ftpClient.isConnected()) {
                return;
            }

            // 连接服务器
            ftpClient.connect(fileInfo.getHost(), fileInfo.getPort() > 0 ? fileInfo.getPort() : 21);

            // 检查连接响应
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftpClient.disconnect();
                throw new RuntimeException("FTP server connection failed, response code: " + replyCode);
            }

            // 登录
            if (!ftpClient.login(fileInfo.getUsername(), fileInfo.getPassword())) {
                ftpClient.disconnect();
                throw new RuntimeException("FTP login failed");
            }

            // 设置传输模式为二进制
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            // 使用被动模式
            ftpClient.enterLocalPassiveMode();

        } catch (IOException e) {
            throw new RuntimeException("FTP connection exception", e);
        }
    }

    public void disconnect() {
        release(false);
    }

    /**
     * 断开FTP连接，不要直接调用这个方法，如果需要手动释放连接，调用release方法，如果直接调用，会导致自动回收中的对象没有被销毁
     */
    public void release(boolean isForce) {
        if(!isForce) {
            AutoReleaser.updateLastOperateTime(this, true);
        }
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                // 断开连接异常不抛出，仅记录
                System.err.println("FTP disconnection exception: " + e.getMessage());
            }
        }
    }


    @Override
    public InputStream getFile(String filePath) {
        try {
            connect();
            return ftpClient.retrieveFileStream(buildFullPath(fileInfo.getRootDir(), filePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve FTP file stream: " + filePath, e);
        }
    }

    @Override
    public void removeTo(String sourceFilePath, String targetFilePath) {
        try {
            connect();
            String source = buildFullPath(fileInfo.getRootDir(), sourceFilePath);
            String target = buildFullPath(fileInfo.getRootDir(), targetFilePath);

            // FTP协议本身不直接支持移动操作，通过重命名实现
            if (!ftpClient.rename(source, target)) {
                throw new RuntimeException("FTP file move failed: " + source + " -> " + target);
            }
        } catch (IOException e) {
            throw new RuntimeException("FTP file move exception", e);
        }
    }

    @Override
    public List<String> getFileNames(String fileDir, FileType fileType) {
        try {
            connect();
            String dir = buildFullPath(fileInfo.getRootDir(), fileDir);
            FTPFile[] files = ftpClient.listFiles(dir);

            List<String> fileNames = new ArrayList<>();
            if (files != null) {
                for (FTPFile file : files) {
                    switch (fileType) {
                        case FILE:
                            if (file.isFile()) {
                                fileNames.add(file.getName());
                            }
                            break;
                        case DIRECTORY:
                            if (file.isDirectory()) {
                                fileNames.add(file.getName());
                            }
                            break;
                        case ALL:
                            fileNames.add(file.getName());
                            break;
                    }
                }
            }
            return fileNames;
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve FTP file list: " + fileDir, e);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            connect();
            if (!ftpClient.deleteFile(buildFullPath(fileInfo.getRootDir(), filePath))) {
                throw new RuntimeException("FTP file deletion failed: " + filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("FTP file deletion exception", e);
        }
    }

    @Override
    public void writeFile(byte[] data, String filePath) {
        try (InputStream is = new ByteArrayInputStream(data)) {
            connect();
            String fullPath = buildFullPath(fileInfo.getRootDir(), filePath);

            // 创建父目录
            createDir(fullPath);

            if (!ftpClient.storeFile(fullPath, is)) {
                throw new RuntimeException("FTP file upload failed: " + filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("FTP file upload exception", e);
        }
    }

    @Override
    public void createDir(String dirName) {
        try {
            connect();
            String fullDir = buildFullPath(fileInfo.getRootDir(), dirName);

            // 处理多级目录
            String[] dirs = fullDir.split("/");
            String currentDir = "";
            for (String dir : dirs) {
                if (dir.isEmpty()) continue;
                currentDir += "/" + dir;
                if (!ftpClient.changeWorkingDirectory(currentDir)) {
                    if (!ftpClient.makeDirectory(currentDir)) {
                        throw new RuntimeException("FTP directory creation failed: " + currentDir);
                    }
                    ftpClient.changeWorkingDirectory(currentDir);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("FTP directory creation exception", e);
        }
    }

    @Override
    public boolean exists(String filePath, boolean throwException) {
        boolean ok;
        String full = buildFullPath(fileInfo.getRootDir(), filePath);
        try {
            connect();
            FTPFile f = ftpClient.mlistFile(full);
            ok = f != null && (f.isFile() || f.isSymbolicLink() || f.isDirectory());
            if (!ok) {
                org.apache.commons.net.ftp.FTPFile[] arr = ftpClient.listFiles(full);
                ok = arr != null && arr.length > 0;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (!ok && throwException) {
            throw new RuntimeException("FTPS path not exists: " + full);
        }
        return ok;
    }

    @Override
    public boolean removeSelf() {
        return true;
    }

}
