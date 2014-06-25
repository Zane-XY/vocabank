$(function() {

$(document).ajaxStart(function() {
    $("#dicts").children().remove();
    $("#dicts").append(
        $("<div class=\"row center-block centered\"><li class=\"fa fa-refresh fa-spin\"></li> </div>")
    );
});

$(document).ajaxStop(function() {
    $("#dicts .fa-spin").remove();
});

function dPanel(heading, content) {
    var p = $("<div class=\"panel panel-default\"> \
                    <div class=\"panel-heading\">"+ heading + "</div> \
               </div>");
    var pbody = $("<div class=\"panel-body\"></div>").append(content);
    return p.append(pbody);
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