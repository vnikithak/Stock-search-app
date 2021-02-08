const express = require('express');
var cors = require('cors');

const fetch = require("node-fetch");
const app = express();
app.use(cors());

const debug = require('debug')('myapp:server');
const PORT = process.env.PORT || 8080;


async function makeRequest(url,requestOptions)
{
  let response = await fetch(url,requestOptions);
  if (response.status === 200) {
      let data = await response.json();
      return data;
  }
}

function getCompanyDetails(symbol){
  var url = "https://api.tiingo.com/tiingo/daily/";
  url+=symbol+"?token=86d9f7f817c5ae691eae746fee742c73d5094e61";
  var requestOptions = {
    'method':'GET',
    'headers':{
      'Content-Type':'application/json'
    }
  };
  body = makeRequest(url,requestOptions);
  return body;
}

function getCompanySummary(symbol){
  var url = "https://api.tiingo.com/iex/?tickers=";
  url+=symbol + "&token=86d9f7f817c5ae691eae746fee742c73d5094e61"
  var requestOptions = {
    'method':'GET',
    'headers':{
      'Content-Type':'application/json'
    }
  };
  body = makeRequest(url,requestOptions);
  return body;
}

function getFormattedDate(date){
  var dd = date.getDate();
  var mm = date.getMonth()+1;
  var yyyy = date.getFullYear();
  var formattedDate = dd<10? ('0'+dd):dd;
  var formattedMonth = mm<10? ('0'+mm):mm;
  today = yyyy+'-'+formattedMonth+'-'+formattedDate;
return today;
}

function getBusinessDay(){
  var date = new Date();
  var day = date.getDay();
  if(day === 0)
    date.setDate(date.getDate() - 2)
  if(day===6)
  date.setDate(date.getDate() - 1)
  return getFormattedDate(date)
  }

function getChartTabData(symbol){
  var d = new Date();
var pastYear = d.getFullYear() - 2;
d.setFullYear(pastYear);
  var url = "https://api.tiingo.com/tiingo/daily/"+symbol+"/prices?startDate="+getFormattedDate(d)+"&resampleFreq=daily&token=86d9f7f817c5ae691eae746fee742c73d5094e61";
  var requestOptions = {
    'method':'GET',
    'headers':{
      'Content-Type':'application/json'
    }
  };
  body = makeRequest(url,requestOptions);
  return body;
}

function getCompanyNews(symbol){
  var url = "https://newsapi.org/v2/everything?apiKey=f0b013f6247d43ce885f8b07a31480c1&q="+symbol;
  var requestOptions = {
    'method':'GET',
    'headers':{
      'Content-Type':'application/json'
    }
  };
  body = makeRequest(url,requestOptions);
  return body;
}

app.get('/details/charts/ticker=:ticker', async function (req, res, next)
	{
        var symbol = req.params.ticker;
        var chartTabData = await getChartTabData(symbol);
        var ohlc=[];
        var volume=[];
        for (var i=0; i < chartTabData.length; i += 1) {
          var dateVar = new Date(((chartTabData[i]['date'].split('T'))[0])).getTime()
          ohlc.push([
              dateVar, // the date
              chartTabData[i]['open'], // open
              chartTabData[i]['high'], // high
              chartTabData[i]['low'], // low
              chartTabData[i]['close'] // close
          ]);
  
          volume.push([
              dateVar, // the date
              chartTabData[i]['volume'] // the volume
          ]);
      }
        res.status(200).json({'valid' : 'true', 'ohlc':ohlc,'volume':volume});

})

app.get('/details/news/ticker=:ticker', async function (req, res, next)
	{
        var symbol = req.params.ticker;
        var n = await getCompanyNews(symbol);
        var newsData = n['articles'];
        var responseArray=[];
        for(var i=0;i<newsData.length; i++ ){
          var date1 = new Date(newsData[i]['publishedAt']).getTime();
          var date2 = new Date().getTime();
          var seconds = Math.floor((date2 - (date1))/1000);
          var minutes = Math.floor(seconds/60);
          var hours = Math.floor(minutes/60);
          var days = Math.floor(hours/24);
          var daysAgo="";
          if(minutes<60)
            daysAgo=minutes+" minutes ago";
          else if(hours<24)
            daysAgo=hours+" hours ago";
          else
            daysAgo=days+" days ago";
          responseArray.push({
            'publisher':newsData[i]['source']['name'],
            'publishedDate':newsData[i]['publishedAt'],
            'title':newsData[i]['title'],
            'description':newsData[i]['description'],
            'image':newsData[i]['urlToImage'],
            'url':newsData[i]['url'],
            'daysAgo':daysAgo
          });
        }
        res.status(200).json({'valid' : 'true', 'news':responseArray});

})

app.get('/details/summary/ticker=:ticker', async function (req, res, next)
{
        var symbol = req.params.ticker;
        var companyDetails = await getCompanyDetails(symbol);
        if(companyDetails==null || (companyDetails['detail'] && (companyDetails['detail']==='Not found.')))
        {
            res.status(200).json({'valid':'false'})
        }
        else{
          var s = await getCompanySummary(symbol);
          var summaryDetails = s[0];
          var change = summaryDetails['last']-summaryDetails['prevClose'];
          var changePercent = (change*100)/summaryDetails['prevClose'];
          var high=0.00;
          var low=0.00;
          var mid=0.00;
          var open = 0.00;
          var bidPrice = 0.00;
          var volume= 0.00;
          if(summaryDetails['high']!=null)
            high = summaryDetails['high']
          if(summaryDetails['low']!=null)
            low = summaryDetails['low']
          if(summaryDetails['mid']!=null)
            mid = summaryDetails['mid']
          if(summaryDetails['open']!=null)
            open = summaryDetails['open']
          if(summaryDetails['bidPrice']!=null)
            bidPrice = summaryDetails['bidPrice']
          if(summaryDetails['volume']!=null)
            volume = summaryDetails['volume']
          res.status(200).json({
            'valid' : 'true', 
            'ticker':companyDetails['ticker'],
            'companyName':companyDetails['name'],
            'exchangeCode':companyDetails['exchangeCode'],
            'last':+summaryDetails['last'].toFixed(2),
            'change':+change.toFixed(2),
            'changePercent':+changePercent.toFixed(2),
            'high':+high.toFixed(2),
            'low':+low.toFixed(2),
            'mid':+mid.toFixed(2),
            'open':+open.toFixed(2),
            'bidPrice':+bidPrice.toFixed(2),
            'volume':+volume.toFixed(2),
            'description':companyDetails['description']
            });
        }
})

app.get('/watchlist/tickers=:tickers?', async function (req, res, next)
	{
        var watchlistData=[];
        if(req.params.tickers==null)
          res.status(200).json({'valid' : 'true', 'watchlistData':watchlistData });
        else{
          var symbols = req.params.tickers.split(',');
          for(var i=0;i<symbols.length;i++){
            var companyDetails = await getCompanyDetails(symbols[i]);
            var companySummary = await getCompanySummary(symbols[i]);
            if(companyDetails==null || companySummary==null)
              continue;
          watchlistData.push({
              ticker:companyDetails.ticker!=null?companyDetails.ticker:'',
              name:companyDetails.name!=null?companyDetails.name:'',
              last:companySummary[0].last!=null?(+(companySummary[0].last).toFixed(2)):0.00,
              change:(companySummary[0].last!=null && companySummary[0].prevClose!=null)?(+(companySummary[0].last-companySummary[0].prevClose).toFixed(2)):0.00,
            });
          }
          res.status(200).json({'valid' : 'true', 'watchlistData':watchlistData });
        }

})

app.get('/portfolio/tickers=:tickers?', async function (req, res, next)
	{
        var portfolioData=[];
        if(req.params.tickers==null)
        res.status(200).json({'valid' : 'true', 'portfolioData':portfolioData });
        else{
          var symbols = req.params.tickers.split(',');
        for(var i=0;i<symbols.length;i++){
          var companyDetails = await getCompanyDetails(symbols[i]);
          var companySummary = await getCompanySummary(symbols[i]);
          if(companyDetails==null || companySummary==null)
            continue;
           portfolioData.push({
            ticker:companyDetails.ticker!=null?companyDetails.ticker:'',
            name:companyDetails.name!=null?companyDetails.name:'',
            last:companySummary[0].last!=null?(+(companySummary[0].last).toFixed(2)):0.00,
            change:(companySummary[0].last!=null && companySummary[0].prevClose!=null)?(+(companySummary[0].last-companySummary[0].prevClose).toFixed(2)):0.00,
          });
        }
        res.status(200).json({'valid' : 'true', 'portfolioData':portfolioData });
        }

})

app.get('/autocomplete/ticker=:ticker?', async function (req, res, next)
{
  if(!req.params || req.params.ticker==null || req.params.ticker=='' || req.params.ticker==' ')
  {
    res.status(200).json({'valid':'true','autocomplete':JSON.stringify([])});
  }
  else{
    var symbol = req.params.ticker;
  var url = "https://api.tiingo.com/tiingo/utilities/search?query="+symbol+'&token=86d9f7f817c5ae691eae746fee742c73d5094e61';
  var requestOptions = {
    'method':'GET',
    'headers':{
      'Content-Type':'application/json'
    }
  };
  body = await makeRequest(url,requestOptions);
  if(body==null)
  res.status(200).json({'valid':'true','autocomplete':JSON.stringify([])});
  res.status(200).json({'valid':'false', 'autocomplete':body});
  }

})

app.listen(PORT, () => console.log(`Example app listening on port ${PORT}!`))
//404 Responses
app.use(function (req, res, next) {
  res.status(404).json({"message" : "Sorry can't find that resource!"})
})

//Error Handling
app.use(function (err, req, res, next) {
  console.error(err.stack)
  res.status(500).json({"message" : 'Some Error at the Server Side!'})
})

module.exports = app;
