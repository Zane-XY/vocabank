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
        return r;
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
        $("#entryContextHtml").html("");
        $('#entryModal').foundation('reveal', 'open');
    });

    $("#entryForm").submit(function (e) {
        var data = flattenForm("#entryForm");
        data['context'] = $("#entryContextHtml").html();
        console.log($("#entryContextHtml").text());
        $.ajax({
                contentType: 'application/json',
                type: "POST",
                url: "/entry/save",
                dataType: 'json',
                data: JSON.stringify(data),
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
        var entry = $(this).closest("entry-data");
        $.ajax({
                url: "/entry/delete",
                type: "POST",
                data: JSON.stringify({
                        id: parseInt($(entry).attr("entryId"))
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
        var entry = $(this).closest("entry-data");
        entry.find(".entrySaveIcon").removeClass("hide");
        entry.find(".entryEditIcon").addClass("hide");
        entry.find(".editable").attr("contenteditable", "true");
    });

    $(document).on("click", ".entrySave", function (e) {
        var entry = $(this).closest("entry-data");
        var entryData = {
            id: parseInt($(entry).attr("entryId")),
            headword: entry.find(".entry-headword").text(),
            context: entry.find(".entry-context").html()
        };
        $.ajax({
                contentType: 'application/json',
                type: "POST",
                url: "/entry/save",
                dataType: 'json',
                data: JSON.stringify(entryData),
                success: function () {
                    entry.find(".editable").attr("contenteditable", "false");
                    entry.find(".entrySaveIcon").addClass("hide");
                    entry.find(".entryEditIcon").removeClass("hide");
                },
                error: function (jqXHR) {
                    $("#infoModalContent").text(JSON.stringify(jqXHR.responseJSON));
                    $('#infoModal').foundation('reveal', 'open');
                }
        });

        e.preventDefault();
    });

    $(document).foundation({
            abide : {
                live_validate : true,
                focus_on_invalid : true
            }
    });

});
