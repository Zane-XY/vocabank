 $(function() {
     $('div.rating').raty({
          path: '/assets/libs/raty/images',
          targetScore: '#rating_target',
          scoreName: 'rating',
          score: $("input#rating_target").val()
      });

     $( "#lookupBtn" ).click(function() {
        $("#def").load("/entry/lookupDef/" + $("input#title").val());
     });

 });