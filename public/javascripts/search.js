$(function() {

$(document).ajaxStart(function() {
    $("#dicts").children().remove();
    $("#loadingStatus").css('visibility','visible');
});

$(document).ajaxStop(function() {
    $("#top_spacer").children().remove();
    $("#loadingStatus").css('visibility','hidden');
});

$(document).on("click", ".audio_play_button", function (e) {
    new Audio($(this).attr('data-src-mp3')).play();
    e.preventDefault();
});

function dPanel(heading, content) {
    return $("<div>").addClass("row")
                     .append($("<div>").addClass("large-10 large-centered columns")
                                       .append($("<div>").addClass("panel")
                                                         .prepend("<p class=\"panel-heading\">" + heading + "</p>")
                                                         .append(content)));
}

$("#search_form").submit(function(e) {
    var term = $("#search_box").val();
    if(term.match(/(\w|\s)+/) && term.length < 30) {
        $.get("/lookup/longman/" + term, function (r) {
            $("#dicts").append(dPanel("Longman Dictionary of Contemporary English",r).addClass("longman"));
        }, "html");

        $.get("/lookup/oxford/" + term, function (r) {
            $("#dicts").append(dPanel("Oxford Leaner's Dictionary",r).addClass("oxford"));
        }, "html");

        $.get("/lookup/cambridge/" + term, function (r) {
            $("#dicts").append(dPanel("Cambridge Advanced Learner's Dictionary",r).addClass("cambridge"));
        }, "html");
    }
    e.preventDefault();
});



$('#search_box').autocomplete({
    paramName: 'q',
    serviceUrl: '/autocomplete',
    triggerSelectOnValidInput: false,
    minChars: 2,
    transformResult: function(r) {
        return {suggestions: $.parseJSON(r)};
    },
    onSelect: function(e){ $("#search_form").submit();}
})

});