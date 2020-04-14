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