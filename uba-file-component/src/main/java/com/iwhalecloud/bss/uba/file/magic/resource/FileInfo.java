package com.iwhalecloud.bss.uba.file.magic.resource;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.ssssssss.magicapi.core.model.MagicEntity;

@EqualsAndHashCode(callSuper = true)
@Data
public class FileInfo extends MagicEntity {

    public static enum FileType{
        local, ftp, ftps, sftp
    }

    /**文件服务器的唯一键*/
    private String key;
    /**文件类型，可选值：local / ftp / ftps / sftp */
    private FileType fileType;

    // 服务器地址
    private String host;
    // 服务器端口（默认 22）
    private int port;
    // 用户名
    private String username;
    // 密码（或使用密钥认证）
    private String password;
    // 公钥字符串
    private String publicKey;
    // 超时时间
    private int timeout = 30000;
    // 根目录
    private String rootDir;

    // 安全相关配置
    // private String publicKeyPath; // 公钥路径
    private String privateKeyPath;// 私钥路径
    private String privateKeyContent;// 私钥内容
    private String passphrase;    // 私钥密码
    private boolean isProduction; // 是否生产环境

    private String knownHostsPath; // 客户端曾经连接过的 SSH/SFTP 服务器的身份信息（公钥指纹）文件存储位置
    private String knownHostsContent;// 客户端曾经连接过的 SSH/SFTP 服务器的身份信息（公钥指纹）内容

    public FileInfo(){}

    /**初始化SFTP的配置*/
    public FileInfo(String host, int port, String username, String password, String publicKey, String knownHostsPath){
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.publicKey = publicKey;
        this.knownHostsPath = knownHostsPath;
    }
}
