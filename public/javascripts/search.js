$(function() {

$(document).ajaxStart(function() {
    $("#dicts").children().remove();
    $("#loadingStatus").removeClass("hide");
});

$(document).ajaxStop(function() {
    $("#top_spacer").children().remove();
    $("#loadingStatus").addClass("hide");
});

function dPanel(heading, content) {
    return $("<div>").addClass("row")
                     .append($("<div>").addClass("large-10 large-centered columns")
                                       .prepend("<h6>" + heading + "</h6>")
                                       .append($("<div>").addClass("panel")
                                                         .append(content)));
}

$("#search_form").submit(function(e) {
    var term = $("#search_box").val();
    if(term.match(/(\w|\s)+/) && term.length < 30) {
        $.get("/lookup/longman/" + term, function (r) {
            $("#dicts").append(dPanel("Longman D",r));
        }, "html");

        $.get("/lookup/oxford/" + term, function (r) {
            $("#dicts").append(dPanel("Oxford Learner",r));
        }, "html");

        $.get("/lookup/cambridge/" + term, function (r) {
            $("#dicts").append(dPanel("Cambridge Learner",r));
        }, "html");
    }
    e.preventDefault();
});

});