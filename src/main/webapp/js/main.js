$(document).ready(function(){
	"use strict";
	
	$("textEntry").focus();

	$("#textEntrySubmit").click (function () {
		tag($("#textEntry").val());
	});

	$("#textEntry").keypress(function(event) {
	    if (event.which == 13) {
	        event.preventDefault();
	        var sentence = $("#textEntry").val();
	        tag(sentence);
	        
	        // clear the input textbox
	        $("#textEntry").val("");
	        
	    }
	});

	function tag(text){
	    $.getJSON('CQArelated'+'?jsonp=?', {'text' : text},
		      function(items){
	    		console.log("got related questions");
	    		console.log(items);
	    		$("#resultArea").html("<h1>"+text+"</h1>");
	    	    for(var i=0; i<items.length; i++){
	    	    	var item = items[i];
	    	    	var question = item.question;
	    	    	var comments = item.comments;
	    	    	comments.sort(function(c1, c2){
	    	    		return c2.score-c1.score;
	    	    	});
	    	    	$("#resultArea").append("<h2>"+item.question.subject+"</h2>");
	    	    	$("#resultArea").append(item.question.body+"<p>");
	    	    	for(var j=0; j<comments.length; j++){
	    	    		var comment = comments[j];
	    	    		$("#resultArea").append(comment.score+"<br>");
	    	    		$("#resultArea").append(comment.wholeText+"<p>");
	    	    	}
	    	    }

	    });
	}

})
