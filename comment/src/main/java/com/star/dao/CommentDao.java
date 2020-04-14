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
    List<Comment> findByParentIdNotNull(@Param("id") Long id);

    //查询二级以及所有子集回复
    List<Comment> findByReplayId(@Param("childId") Long childId);

}