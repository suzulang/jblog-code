package com.example.myblog.common;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.DownloadUrl;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * 七牛云工具类
 */
@Component
public class QiniuUtils {

    public  static String accessKey;
    public  static String secretKey;
    public  static String bucket;

    public QiniuUtils(@Value("${qiniu.access.key}") String accessKey,
                      @Value("${qiniu.secret.key}") String secretKey,
                      @Value("${qiniu.bucket.name}") String bucket) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucket;
    }

    // 上传Base64编码的文件
    public static void upload2Qiniu(String base64Data, String fileName) {
        // 检查Base64字符串是否包含前缀，并去除
        String base64Image = base64Data;
        if (base64Data.contains(",")) {
            base64Image = base64Data.substring(base64Data.indexOf(",") + 1);
        }

        // 将Base64字符串解码为字节数组
        byte[] bytes = Base64.getDecoder().decode(base64Image);

        // 构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone2());
        UploadManager uploadManager = new UploadManager(cfg);
        String key = fileName; // 使用文件名作为key
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);

        try {
            Response response = uploadManager.put(bytes, key, upToken);
            // 解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
        } catch (QiniuException ex) {
            Response r = ex.response;
            System.err.println(r.toString());
            try {
                System.err.println(r.bodyString());
            } catch (QiniuException ex2) {
                // ignore
            }
        }
    }


    /**
     * 测试方法：生成七牛云上文件的下载链接
     *
     * @param key       要下载的文件在七牛云存储中的键（文件名）
     */
    public static String getDownloadUrl(String key) {
        String domain = "cdn.jtp26.vip";
        boolean useHttps = true;
        DownloadUrl url = new DownloadUrl(domain, useHttps, key);

        // 设置链接的有效期为半年
        long expireInSeconds = 180 * 24 * 3600; // 180天 * 24小时/天 * 3600秒/小时
        long deadline = System.currentTimeMillis() / 1000 + expireInSeconds;

        // 使用七牛云的AccessKey和SecretKey创建Auth对象
        Auth auth = Auth.create(QiniuUtils.accessKey, QiniuUtils.secretKey);

        String urlString = null;
        try {
            // 构建带有签名的下载URL
            urlString = url.buildURL(auth, deadline);
        } catch (QiniuException e) {
            e.printStackTrace();
        }

        System.out.println(urlString);
        return urlString;
    }


    //删除文件
    public static void deleteFileFromQiniu(String fileName){
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone0());
        String key = fileName;
        Auth auth = Auth.create(accessKey, secretKey);
        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            bucketManager.delete(bucket, key);
        } catch (QiniuException ex) {
            //如果遇到异常，说明删除失败
            System.err.println(ex.code());
            System.err.println(ex.response.toString());
        }
    }

}
