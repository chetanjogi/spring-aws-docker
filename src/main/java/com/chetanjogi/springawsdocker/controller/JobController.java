package com.chetanjogi.springawsdocker.controller;


import com.chetanjogi.springawsdocker.model.JobPost;
import com.chetanjogi.springawsdocker.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin
public class JobController {

    @Autowired
    private JobService service;

    @GetMapping("load")
    public String loadData(){
        return service.loadData();
    }

    @GetMapping("jobPosts")
    public List<JobPost> getAllJobs(){
        return service.getAllJobs();
    }

    @GetMapping("jobPost/{postId}")
    public JobPost getJob(@PathVariable int postId){
        return  service.getJob(postId);
    }

    @PostMapping("/jobPost")
    public JobPost addJob(@RequestBody JobPost jobPost){
        System.out.println("called post mapping");
        service.addJob(jobPost);
        return service.getJob(jobPost.getPostId());
    }

    @PutMapping("/jobPost")
    public JobPost updateJob(@RequestBody JobPost jobPost){
        service.updateJob(jobPost);
        return service.getJob(jobPost.getPostId());
    }

    @DeleteMapping("jobPost/{postId}")
    public void deleteJob(@PathVariable int postId){
        service.deleteJob(postId);
    }


    @GetMapping("jobPosts/keyword/{keyword}")
    public List<JobPost> search(@PathVariable String keyword){
        return service.search(keyword);
    }

}
