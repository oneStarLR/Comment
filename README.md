### 零、效果
在网上搜索了很多，发现很多都是用两张表或者使用jpa实现的，本篇文章将讲述使用一张表来实现评论回复楼中楼功能，使用Mybatis作为持久层框架，有图有真相，先来看看最终效果

![image](https://note.youdao.com/yws/api/personal/file/ECF19CBD825F452EA46EB8D0B7890378?method=download&shareKey=4496729e44aba73a276a1b075b5e5b9c)

### 一、数据库设计
首先来看看有哪些字段，既然是评论回复，你觉得应该有哪些字段呢，带着功能去思考这个问题
> 首先是主键（id），既然是评论，必须要有评论人的姓名（nickname），为了以后能联系到评论人，需要评论人的邮箱（email），然后就是评论内容（content），为了方便显示，还需要评论人的头像（avatar），评论的时间当然少不了（create_time），既然是一张表，要如何确定回复的所属关系呢，那就需要知道是回复谁的评论（parent_comment_id）。bingo！这些字段就OK了，可能你会问，那回复怎么办，问题不大，可以在实体类中创建一个回复评论的集合replyComments，用来存储回复消息。

表结构如下：




建表语句如下：

```sql
create database comment;

DROP TABLE IF EXISTS `comment`;

CREATE TABLE `comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nickname` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `parent_comment_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;
```


### 二、搭建SpringBoot框架
1. 如图，选择相应的组件，创建SpringBoot项目

![image](https://note.youdao.com/yws/api/personal/file/9382E293A4BA457CA63A36AE5818C366?method=download&shareKey=bf10961f80234493b74736e98768be17)

2. 将application.properties配置文件改成yml后缀，即application.yml，主要对thymeleaf模板、数据库、mybatis、评论头像进行配置，配置如下：


```yml
spring:
#配置thymeleaf模板
  thymeleaf:
    mode: HTML
#配置数据库
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/comment?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC
    username: root
    password: 806188

#配置mybatis
mybatis:
  type-aliases-package: com.star.entity
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true

#配置评论头像
comment.avatar: /images/avatar.png
```

### 三、代码编写
#### 1. 实体类
创建实体类，这里除了字段和回复评论列表外父级评论和父级评论姓名的变量，用来设置父级评论的id和显示父级评论姓名的
```java
package com.star.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description: 评论实体类
 * @Date: Created in 11:02 2020/4/14
 * @Author: ONESTAR
 * @QQ: 316392836
 * @URL: https://onestar.newstar.net.cn/
 */
public class Comment {

    private Long id;
    private String nickname;
    private String email;
    private String content;
    private String avatar;
    private Date createTime;
    private Long parentCommentId;

    //回复评论
    private List<Comment> replyComments = new ArrayList<>();
    private Comment parentComment;
    private String parentNickname;
    
    //省去了get和set还有toString方法

}
```

#### 2. 业务层接口
创建业务层service接口，主要是查询评论列表和存储评论两个接口

```java
package com.star.service;

import com.star.entity.Comment;

import java.util.List;

/**
 * @Description: 评论业务层接口
 * @Date: Created in 11:48 2020/4/14
 * @Author: ONESTAR
 * @QQ: 316392836
 * @URL: https://onestar.newstar.net.cn/
 */
public interface CommentService {

    //查询评论列表
    List<Comment> listComment();

    //保存评论
    int saveComment(Comment comment);

}
```

#### 3. 持久层接口
创建持久层dao接口，只要有添加评论、查询父级评论、查询一级回复、查询二级以及所有子集回复

```java
package com.star.dao;

import com.star.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description: 评论持久层接口
 * @Date: Created in 12:06 2020/4/14
 * @Author: ONESTAR
 * @QQ: 316392836
 * @URL: https://onestar.newstar.net.cn/
 */
@Mapper
@Repository
public interface CommentDao {

    //添加一个评论
    int saveComment(Comment comment);

    //查询父级评论
    List<Comment> findByParentIdNull(@Param("ParentId") Long ParentId);

    //查询一级回复
    List<Comment> findParentIdNotNull(@Param("id") Long id);

    //查询二级以及所有子集回复
    List<Comment> findByReplayId(@Param("childId") Long childId);

}
```

#### 4. Mapper
创建CommentDao.xml文件，主要是添加评论、查询父级评论、查询一级回复、查询二级以及所有子集评论的SQL语句

```xml
<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.star.dao.CommentDao">

    <!--添加评论-->
    <insert id="saveComment" parameterType="com.star.entity.Comment">
        insert into comment.comment (nickname,email,content,avatar,create_time,parent_comment_id)
        values (#{nickname},#{email},#{content},#{avatar},#{createTime},#{parentCommentId});
    </insert>

    <!--查询父级评论-->
    <select id="findByParentIdNull" resultType="com.star.entity.Comment">
        select *
        from comment.comment c
        where c.parent_comment_id = #{ParentId}
        order by c.create_time desc
    </select>

    <!--查询一级回复-->
    <select id="findByParentIdNotNull" resultType="com.star.entity.Comment">
        select *
        from comment.comment c
        where c.parent_comment_id = #{id}
        order by c.create_time desc
    </select>

    <!--查询二级以及所有子集回复-->
    <select id="findByReplayId" resultType="com.star.entity.Comment">
        select *
        from comment.comment c
        where c.parent_comment_id = #{childId}
        order by c.create_time desc
    </select>

</mapper>

```

#### 5. 控制器CommentController

```java
package com.star.controller;

import com.star.entity.Comment;
import com.star.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @Description: 评论控制器
 * @Date: Created in 14:30 2020/4/14
 * @Author: ONESTAR
 * @QQ: 316392836
 * @URL: https://onestar.newstar.net.cn/
 */
@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Value("${comment.avatar}")
    private String avatar;

    @GetMapping("/")
    public String comment() {
        return "comment";
    }

    @GetMapping("/comment")
    public String comments(Model model) {
        List<Comment> comments = commentService.listComment();
        model.addAttribute("comments", comments);
        return "comment :: commentList";
    }

    @PostMapping("/comment")
    public String post(Comment comment) {
        //设置头像
        comment.setAvatar(avatar);
        if (comment.getParentComment().getId() != null) {
            comment.setParentCommentId(comment.getParentComment().getId());
        }
        commentService.saveComment(comment);
        return "redirect:/comment";
    }
}
```

#### 6. 接口实现类
逻辑都在接口实现类CommentServiceImpl里面实现，咱们来梳理一下逻辑（逻辑没梳理好，代码永远都是乱的，逻辑是第一步！！！），添加评论直接insert就可以了，这里主要讲一下查询：
1. 根据id为“-1”查询出所有父评论
2. 根据父评论的id查询出一级子回复
3. 根据子回复的id循环迭代查询出所有子集回复
4. 将查询出来的子回复放到一个集合中

要注意将父评论的姓名给set进去，代码如下：

```java
package com.star.service.impl;

import com.star.dao.CommentDao;
import com.star.entity.Comment;
import com.star.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @Date: Created in 13:44 2020/4/14
 * @Author: ONESTAR
 * @QQ: 316392836
 * @URL: https://onestar.newstar.net.cn/
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentDao commentDao;

    //存放迭代找出的所有子代的集合
    private List<Comment> tempReplys = new ArrayList<>();

    /**
     * @Description: 查询评论
     * @Auther: ONESTAR
     * @Date: 17:26 2020/4/14
     * @Param:
     * @Return: 评论消息
     */
    @Override
    public List<Comment> listComment() {
        //查询出父节点
        List<Comment> comments = commentDao.findByParentIdNull(Long.parseLong("-1"));
        for(Comment comment : comments){
            Long id = comment.getId();
            String parentNickname1 = comment.getNickname();
            List<Comment> childComments = commentDao.findByParentIdNotNull(id);
            //查询出子评论
            combineChildren(childComments, parentNickname1);
            comment.setReplyComments(tempReplys);
            tempReplys = new ArrayList<>();
        }
        return comments;
    }

    /**
     * @Description: 查询出子评论
     * @Auther: ONESTAR
     * @Date: 17:31 2020/4/14
     * @Param: childComments：所有子评论
     * @Param: parentNickname1：父评论的姓名
     * @Return:
     */
    private void combineChildren(List<Comment> childComments, String parentNickname1) {
        //判断是否有一级子回复
        if(childComments.size() > 0){
            //循环找出子评论的id
            for(Comment childComment : childComments){
                String parentNickname = childComment.getNickname();
                childComment.setParentNickname(parentNickname1);
                tempReplys.add(childComment);
                Long childId = childComment.getId();
                //查询二级以及所有子集回复
                recursively(childId, parentNickname);
            }
        }
    }

    /**
     * @Description: 循环迭代找出子集回复
     * @Auther: ONESTAR
     * @Date: 17:33 2020/4/14
     * @Param: childId：子评论的id
     * @Param: parentNickname1：子评论的姓名
     * @Return:
     */
    private void recursively(Long childId, String parentNickname1) {
        //根据子一级评论的id找到子二级评论
        List<Comment> replayComments = commentDao.findByReplayId(childId);

        if(replayComments.size() > 0){
            for(Comment replayComment : replayComments){
                String parentNickname = replayComment.getNickname();
                replayComment.setParentNickname(parentNickname1);
                Long replayId = replayComment.getId();
                tempReplys.add(replayComment);
                //循环迭代找出子集回复
                recursively(replayId,parentNickname);
            }
        }
    }

    @Override
    //存储评论信息
    public int saveComment(Comment comment) {
        comment.setCreateTime(new Date());
        return commentDao.saveComment(comment);
    }
}
```

#### 7. 前端页面comment.html
最后是前端显示页面，这里采用semantic-ui作为UI组件，使用thymeleaf模板解析，记得在static文件夹下创建images文件夹，并放一张名为avatar.png的图片

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.w3.org/1999/xhtml">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>评论楼中楼功能</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/semantic-ui/2.2.4/semantic.min.css">
</head>
<body>

<div id="waypoint" class="m-margin- animated fadeIn">
  <div class="ui container m-opacity box-shadow-max">
    <div  class="ui bottom attached segment">
      <!--评论区域列表-->
      <div id="comment-container"  class="ui teal segment">
        <div th:fragment="commentList">
          <div class="ui threaded comments" style="max-width: 100%;">
            <h3 class="ui dividing header">评论</h3>
            <div class="comment" th:each="comment : ${comments}">
              <a class="avatar">
                <img src="../static/images/avatar.png" th:src="@{${comment.avatar}}">
              </a>
              <div class="content">
                <a class="author" >
                  <span th:text="${comment.nickname}">小红</span>
                </a>
                <div class="metadata">
                  <span class="date" th:text="${#dates.format(comment.createTime,'yyyy-MM-dd HH:mm')}">Today at 5:42PM</span>
                </div>
                <div class="text" th:text="${comment.content}">
                  愿你走出半生，归来仍是少年！
                </div>
                <div class="actions">
                  <a class="reply" data-commentid="1" data-commentnickname="Matt" th:attr="data-commentid=${comment.id},data-commentnickname=${comment.nickname}" onclick="reply(this)">回复</a>
                </div>
              </div>
              <!--子集评论-->
              <div class="comments" th:if="${#arrays.length(comment.replyComments)}>0">
                <div class="comment" th:each="reply : ${comment.replyComments}">
                  <a class="avatar">
                    <img src="../static/images/avatar.png" th:src="@{${reply.avatar}}">
                  </a>
                  <div class="content">
                    <a class="author" >
                      <span th:text="${reply.nickname}">小白</span>
                      &nbsp;<span th:text="|@ ${reply.parentNickname}|" class="m-teal">@ 小红</span>
                    </a>
                    <div class="metadata">
                      <span class="date" th:text="${#dates.format(reply.createTime,'yyyy-MM-dd HH:mm')}">Today at 5:42PM</span>
                    </div>
                    <div class="text" th:text="${reply.content}">
                      你也是！
                    </div>
                    <div class="actions">
                      <a class="reply" data-commentid="1" data-commentnickname="Matt" th:attr="data-commentid=${reply.id},data-commentnickname=${reply.nickname}" onclick="reply(this)">回复</a>
                    </div>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </div>
      </div>
      <div id="comment-form" class="ui form">
        <input type="hidden" name="parentComment.id" value="-1">
        <div class="field">
          <textarea name="content" placeholder="请输入评论信息..."></textarea>
        </div>
        <div class="fields">
          <div class="field m-mobile-wide m-margin-bottom-small">
            <div class="ui left icon input">
              <i class="user icon"></i>
              <input type="text" name="nickname" placeholder="姓名" th:value="${session.user}!=null ? ${session.user.nickname}">
            </div>
          </div>
          <div class="field m-mobile-wide m-margin-bottom-small">
            <div class="ui left icon input">
              <i class="mail icon"></i>
              <input type="text" name="email" placeholder="邮箱" th:value="${session.user}!=null ? ${session.user.email}">
            </div>
          </div>
          <div class="field  m-margin-bottom-small m-mobile-wide">
            <button id="commentpost-btn" type="button" class="ui teal button m-mobile-wide"><i class="edit icon"></i>发布</button>
          </div>
        </div>

      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/jquery@3.2/dist/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/semantic-ui/2.2.4/semantic.min.js"></script>

<script th:inline="javascript">
    //评论表单验证
    $('.ui.form').form({
        fields: {
            title: {
                identifier: 'content',
                rules: [{
                    type: 'empty',
                    prompt: '请输入评论内容'
                }
                ]
            },
            content: {
                identifier: 'nickname',
                rules: [{
                    type: 'empty',
                    prompt: '请输入你的大名'
                }]
            },
            type: {
                identifier: 'email',
                rules: [{
                    type: 'email',
                    prompt: '请填写正确的邮箱地址'
                }]
            }
        }
    });


    $(function () {
        $("#comment-container").load(/*[[@{/comment}]]*/"comment/");
    });

    $('#commentpost-btn').click(function () {
        var boo = $('.ui.form').form('validate form');
        if (boo) {
            console.log('校验成功');
            postData();
        } else {
            console.log('校验失败');
        }
    });

    function postData() {
        $("#comment-container").load(/*[[@{/comment}]]*/"",{
            "parentComment.id" : $("[name='parentComment.id']").val(),
            "nickname": $("[name='nickname']").val(),
            "email"   : $("[name='email']").val(),
            "content" : $("[name='content']").val()
        },function (responseTxt, statusTxt, xhr) {
            // $(window).scrollTo($('#goto'),0);
            clearContent();
        });
    }

    function clearContent() {
        $("[name='nickname']").val('');
        $("[name='email']").val('');
        $("[name='content']").val('');
        $("[name='parentComment.id']").val(-1);
        $("[name='content']").attr("placeholder", "请输入评论信息...");
    }

    function reply(obj) {
        var commentId = $(obj).data('commentid');
        var commentNickname = $(obj).data('commentnickname');
        $("[name='content']").attr("placeholder", "@"+commentNickname).focus();
        $("[name='parentComment.id']").val(commentId);
        $(window).scrollTo($('#comment-form'),500);
    }
</script>
</body>
</html>
```

#### 8. 目录结构

![image](https://note.youdao.com/yws/api/personal/file/5CAF1B63D4564C0D9BC49A1455766863?method=download&shareKey=c012a49d9448e38862cf88723fb1b089)


源码地址，欢迎star：https://github.com/oneStarLR/SpringBoot-Mybatis-
