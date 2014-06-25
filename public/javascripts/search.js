$(function() {

var spinner = $("<div>").append($("<i>").addClass("fa fa-circle-o-notch fa-spin"));

 $( "#search_form" ).submit(function(e) {
    var term = $("#search_box").val();
    if(term.match(/(\w|\s)+/)) {

        $("#longman_container").children().replaceWith(spinner);
        $.load('/lookup/longman/' + term, function (res) {
             $("#longman_container").remove(".fa-spin");
             $("#longman_container").addClass("panel panel-default");
             $("#longman_container")
                 .append($("<div class=\"panel-heading\">Panel heading</div>"))
                 .append($("<div class=\"panel-body\">").wrap(res));
        });

         $("#oxford_container").children().replaceWith(spinner);
         $("#oxford_container").load('/lookup/oxford/' + term, function () {
             $(this).remove(".fa-spin");
             $(this).addClass("panel panel-default");
             $(this).prepend("<div class=\"panel-heading\">Oxford</div>");
         });

         $("#cambridge_container").children().replaceWith(spinner);
         $("#cambridge_container").load('/lookup/cambridge/' + term, function () {
             $(this).remove(".fa-spin");
             $(this).addClass("panel panel-default");
             $(this).prepend("<div class=\"panel-heading\">Cambridge</div>");
         });
    }
    e.preventDefault();
});

});