$(function () {
  function initRaty() {
    $('div.rating').raty({
      path: 'assets/libs/raty/images',
      starType: 'i',
      score: function () {
        return $(this).attr('data-score');
      },
      click: function (score, evt) {
        $.ajax({
          type: "POST",
          url: "/entry/setRating",
          data: JSON.stringify({
            "id": parseInt($(this).attr('data-id')),
            "value": parseInt(score)
          }),
          dataType: "json",
          contentType: "application/json",
          complete: function (data) {}
        });
      }
    });
  };

  initRaty();

  function bindForm(formSelector, json) {
    $.each(json, function (k, v) {
      $(formSelector).find(":input[name='" + k + "']").val(v);
    });
  }

  function flattenForm(formSelector) {
    var r = {};
    $(formSelector).serializeArray().map(function (x) {
      r[x.name] = x.value;
    });
    return JSON.stringify(r);
  }

  function reloadEntryList () {
    $("#entrylistContainer").load("/entries #entrylist", function() {initRaty();});
  }

  function resetEntryForm() {
    var ef = $("#entryForm");
    ef.find("input[name='id']").val("");
    ef.find("input[name='rating']").val("1");
    ef[0].reset();
  }

  $("#newEntry").click(function () {
    resetEntryForm();
    $('#entryModal').foundation('reveal', 'open');
  });

  $("#entryForm").submit(function (e) {
    $.ajax({
      contentType: 'application/json',
      type: "POST",
      url: "/entry/save",
      dataType: 'json',
      data: flattenForm("#entryForm"),
      context: this,
      success: function () {
        $('a.close-reveal-modal').trigger('click');
        reloadEntryList();
      },
      error: function (jqXHR) {
        $('a.close-reveal-modal').trigger('click');
        $("#infoModalContent").text(JSON.stringify(jqXHR.responseJSON));
        $('#infoModal').foundation('reveal', 'open');
      },
      complete: function () {
        resetEntryForm();
      }
    });
    e.preventDefault();
  });

  $(document).on("click", ".entryDel", function (e) {
    $.ajax({
      url: "/entry/delete",
      type: "POST",
      data: JSON.stringify({
        id: parseInt($(this).attr("data"))
      }),
      contentType: "application/json",
      dataType: "json",
      context: this,
      success: function () {
        $(this).closest("div.entry").remove();
      }
    });
    e.preventDefault();
  });

  $(document).on("click", ".entryEdit", function (e) {
    var entry = $(this).closest("div.entry");
    var entryData = {
      id: parseInt($(this).attr("data")),
      headword: entry.find(".entry-headword").text(),
      context: entry.find(".entry-context").text()
    };
    bindForm("#entryForm", entryData);
    $('#entryModal').foundation('reveal', 'open');
    e.preventDefault();
  });

  $(document).foundation({
    abide : {
      live_validate : true,
       focus_on_invalid : true
     }
  });

});