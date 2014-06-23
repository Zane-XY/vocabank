 $(function() {
     $('div.rating').raty({
          path: '/assets/libs/raty/images',
          targetScore: '#rating_target',
          scoreName: 'rating',
          score: $("input#rating_target").val()
      });

    $.get("http://dictionary.cambridge.org/dictionary/learner-english/reward_1?q=reward", function(data) {
        //alert($("div.di").html(data));
        alert(data);
    });
 });