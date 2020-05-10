const express = require("express");
const app = express();
const googleTrends = require('google-trends-api');
//const http = require("http");
const https = require("https");
//const request = require("request");
const url = require('url');
//const cors = require("cors");       //�ǵ�ɾcors
//app.use(cors());
const Guradian_home_url = "https://content.guardianapis.com/search?api-key=e628090b-804b-44f6-8a69-f33efc0bdf06&section=(sport|business|technology|politics)&show-blocks=all";
const Guradian_tab_url1 = "https://content.guardianapis.com/";
const Guradian_tab_url2 = "?api-key=966536f3-4a7b-4214-b08b-ad9bdf0f2f3e&show-blocks=all";

const autosuggest_url = "https://api.cognitive.microsoft.com/bing/v7.0/suggestions?q=9c85981dd8db40a8b68540a9f9141e1a";

const port = process.env.PORT || 7000;
//for test
app.get("/api", function (request, response) {
    response.send("Hi");
})

//suggest search
app.get("/api/Search", function (req, res) {
    var cur_url = "";
    var params = url.parse(req.url, true).query;
    
    cur_url = 'https://content.guardianapis.com/search?q=' + params.keyword + '&api-key=966536f3-4a7b-4214-b08b-ad9bdf0f2f3e&show-blocks=all'
    
    
    //console.log("Search")
    https.get(cur_url, function (response) {
        var res_text = "";
        response.on("data", function (data) {
            res_text += data;
        });
        response.on("end", function () {
            var result = JSON.parse(res_text);
            console.log(result);
            return res.send(result);
        });
    });
})

//Detail page
app.get("/api/Detail", function (req, res) {
    var cur_url = "";
    var params = url.parse(req.url, true).query;
    cur_url = "https://content.guardianapis.com/" + params.link + "?api-key=966536f3-4a7b-4214-b08b-ad9bdf0f2f3e&show-blocks=all";
    
    https.get(cur_url, function (response) {
        var res_text = "";
        response.on("data", function (data) {
            res_text += data;
        });
        response.on("end", function () {
            var result = JSON.parse(res_text);
            console.log(result);
            return res.send(result);
        });
    });
})

app.get("/api/latest",function(req,res){
    let cur_url = 'https://content.guardianapis.com/search?order-by=newest&show-fields=starRating,headline,thumbnail,short-url&api-key=966536f3-4a7b-4214-b08b-ad9bdf0f2f3e';
    https.get(cur_url, function (response) {
        var res_text = "";
        response.on("data", function (data) {
            res_text += data;
        });
        response.on("end", function () {
            var result = JSON.parse(res_text);

            return res.send(result);
        });
    });

});
//general Gurdian source
app.get("/api/Gurdian", function (req, res) {
    var params = url.parse(req.url, true).query;
    var cur_url = "";
    if (params['cate'] == "all") {
        cur_url = Guradian_home_url;
    }
    else {
        cur_url = Guradian_tab_url1 + params['cate'] + Guradian_tab_url2;
    }
    //console.log("Gurdian")
    //console.log(cur_url);
    https.get(cur_url, function (response) {
        var res_text = "";
        response.on("data", function (data) {
            res_text += data;
        });
        response.on("end", function () {
            var result = JSON.parse(res_text);

            return res.send(result);
        });
    });
});
//Gougou trending api
app.get("/api/trending",function(req,res){
    var params = url.parse(req.url, true).query;
    //var cur_url = "";
    console.log(params);
    googleTrends.interestOverTime({keyword:params["data"],startTime:new Date('2019-06-01')})
    .then(function(results){
        //console.log("results: ",results);
        var result = JSON.parse(results);
        return res.send(result)
    })
    .catch(function(err){
        console.error("ERROR",err);
    })

})



app.listen(port, function () {
    console.log("Start");
});
