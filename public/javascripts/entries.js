$(function() {
     $('div.rating').raty({
          path: 'assets/libs/raty/images',
          starType : 'i' ,
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
      $("#entryForm").submit(function (e) {
          $.ajax({
              contentType : 'application/json',
              type: "POST",
              url: "/entry/save",
              dataType: 'json',
              data:JSON.stringify({
                  title: "some title",
                  content: "some content",
                  rating: 1
              }),
              success: function (d) {
                 console.log(d);
              },
              xhrFields: {withCredentials: true}
          });
        e.preventDefault();
      });
});
