package com.zhuofeng.ai_consult_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service public c     private final WebClient weprivate final String knowledgeBaseId;

ic DifyService(@Value("$dify.api.key}"@Value("${dify.api.url}") String apUrl,               @Vthis.knowledgeaeId = knowledgeBaseClient = WebClin.build.baseUrl(apiUrl + "/v1").defaultH 


 **
 * 
 *  
 * @pa
 *     param 
 *     return
 *     
 ub
    System.out.prinln("======================================System.out.println("Uploading file to Dify: " + fileName);System.out.println("Dify API URL: " + webClietbaseUrl())System.out.println("Knowledge base D " + knowledgeBaseId)System.out.println("File path: " + file.eAbsolutePath());System.out.println("File exists:"+ file.exists());System.out.println("File size: " +fle.length());System.out.println("File can rea:" + file.canRea

        .uri("/datasets.contentType(MediTpe.MULTIPART_FOMDATA).body(BodyInserters.fromMultipartData("file        new FileSystemResource(file)))e().onStatus(s    System.out.pinln("Dify upload failed wit status:"  return response.bodyToMono(String.class)        .flatMap(body -> {    System.ou.pireturn Mono.error(new RuntimeException("Diyupload.b.doOnSuccess(response     System.out.printl("iSystem.out.println("======================================");.m

 * 
 * 
 * 
 * @return 是否成功
 */
 ub
    return webClientdelete()        .uri("/datasets/".retrieve().bodyToMono.map(response -> respo

 * 
 * 
 * 
 */
 ub
    return webClient.ge()        .uri("/dataset.retrieve().bodyToMono.map(response -> respo

 * 
 * 
 * 
 * @param userId 用户ID
 * @return 聊天结果
 */
 ub
    Map<String, Object>request  new HashMap<>();request.put"query" query)request.put("user", serId);request.put("stream, false)

        .uri("/chat/com.contentType(MediaType.AP.bodyValue(request).retrieve().bodyToMono.map(response -> respo