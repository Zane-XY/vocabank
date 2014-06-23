$(function() {
    $( "#search_form" ).submit(function(e) {
        var term = $("#search_box").val();
        if(term) {
            $.get('/entry/lookupDef/' + term, function(data){
              $(data).appendTo("#dicts");
            });
        }
        e.preventDefault();
    });
});