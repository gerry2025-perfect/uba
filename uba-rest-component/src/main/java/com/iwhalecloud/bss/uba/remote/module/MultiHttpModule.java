package com.iwhalecloud.bss.uba.remote.module;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import com.iwhalecloud.bss.magic.magicapi.core.annotation.MagicModule;
import com.iwhalecloud.bss.magic.magicapi.modules.http.HttpModule;
import com.iwhalecloud.bss.magic.script.annotation.Comment;

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

    @Comment("Create Connection")
    public MultiHttpModule connect(@Comment(name = "url", value = "Target URL") String url) {
        return new MultiHttpModule(template, url);
    }

    @Comment("Sets the filename and file content in the form parameters. If fileKey is null, it will default to file")
    public HttpModule file(@Comment(name = "fileName", value = "File Name") String fileName,
                           @Comment(name = "fileKey", value = "File Parameter Name on the Server") String fileKey,
                           @Comment(name = "fileContent", value = "File Content") byte[] fileContent) {
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
