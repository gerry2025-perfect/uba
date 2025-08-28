package com.iwhalecloud.bss.uba.rest.magic.module;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.magicapi.modules.http.HttpModule;
import org.ssssssss.script.annotation.Comment;

/**用来进行文件上传的http请求，继承于HttpModule，因此也能发起普通请求*/
@MagicModule("multiHttp")
public class MultiHttpModule extends HttpModule {

    protected RestTemplate template;

    public MultiHttpModule(RestTemplate template) {
        super(template);
        this.template = template;
    }

    public MultiHttpModule(RestTemplate template, String url) {
        super(template, url);
    }

    @Comment("创建连接")
    public MultiHttpModule connect(@Comment(name = "url", value = "目标URL") String url) {
        return new MultiHttpModule(template, url);
    }

    @Comment("设置form参数中文件名和文件内容，如果fileKey为null会默认设置为file")
    public HttpModule file(@Comment(name = "fileName", value = "文件名称") String fileName,
                           @Comment(name = "fileKey", value = "文件在服务器端的参数名") String fileKey,
                           @Comment(name = "fileContent", value = "文件内容") byte[] fileContent) {
        if(fileKey==null || fileKey.isEmpty()){
            fileKey = "file";
        }
        if(fileContent != null && fileName !=null){
            ByteArrayResource resource = new ByteArrayResource(fileContent){
                @Override
                public String getFilename() {
                    // 必须重写getFilename()，否则服务端可能无法获取文件名
                    return fileName;
                }
            };
            data(fileKey, resource);
            contentType(MediaType.MULTIPART_FORM_DATA);
        }
        return this;
    }

}
