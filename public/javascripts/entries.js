$(function() {
     $('div.rating').raty({
          path: 'assets/libs/raty/images',
          score: function() {
            return $(this).attr('data-score');
          },
          click: function(score, evt) {
             $.ajax({
                type: "POST",
                url:  "/entry/setRating",
                data: JSON.stringify({ "id": parseInt($(this).attr('data-id')) , "value": parseInt(score)}),
                dataType : "json",
                contentType: "application/json",
                complete: function (data) {}
             });
          }
      });
});
