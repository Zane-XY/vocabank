 $(function() {
     $('div.rating').raty({
          path: '/assets/libs/raty/images',
          targetScore: '#rating_target',
          scoreName: 'rating',
          score: $("input#rating_target").val()
      });


 });