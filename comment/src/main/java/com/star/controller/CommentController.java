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