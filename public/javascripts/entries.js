$(function () {
    function initRaty() {
        $('.rating').raty({
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
        $("textarea[name='context']").val($("#entryContextHtml").html());
        return true;
    });

    $(".tagLabel").click(function(e) {
        var url = window.location.href.replace(/[?&]p=(\d)*/g, "");
        if(url.match(/t=/)) {
            window.location.replace(url.replace(/(t=[a-zA-Z, ]*)/, "$1," + $(this).text()));
        } else {
            window.location.replace(url + (url.match(/\?/) ? "&"  : "?" ) +  "t=" + $(this).text());
        }
    });

    $(".parseHeadword").click(function () {
        var ctx = $("#entryContextHtml");
        if(ctx.find(":header").length) {
            var h = ctx.find(":header").first();
            $("input[name='headword']").val(h.text());
            h.remove();
        } else if(ctx.find("p").length) {
            var p = ctx.find("p").first();
            $("input[name='headword']").val(p.text());
            p.remove();
        } else {

        }

    });

    $(".playSound").click(function () {
        var entry = $(this).closest("entry-data");
        var headword = entry.find(".entry-headword").text();
        if(!$(this).attr('data')) {
            $.ajax({
                url: "/entry/setSound",
                type: "POST",
                async: false,
                data: JSON.stringify({id: parseInt(entry.attr("entryId")), word: headword}),
                contentType: "application/json",
                dataType: "json",
                context: this,
                success: function (d) {
                    if(d.status == 'OK') {
                        $(this).attr('data', d.sound);
                        $(this).removeClass('fa-volume-off').addClass('fa-volume-up');
                    }
                }
            });
        }
        new Audio($(this).attr('data')).play();
    })

    $(document).on("click", ".entryDel", function (e) {
        var alertModal = $("#alertModal");
        $("h2", alertModal).text("Delete!");
        $("#alertModalContent", alertModal).text("Sure?");
        $(".confirmed", alertModal).text("Delete");
        alertModal.foundation('reveal', 'open');

        var entry = $(this).closest("entry-data");
        $(".confirmed", alertModal).on("click", function(e) {
            alertModal.foundation('reveal', 'close');
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
                        entry.closest("div.entry").remove();
                    }
            });
        });
        e.preventDefault();
    });

    $(document).on("click", ".entryEdit", function () {
        var entry = $(this).closest("entry-data");
        var that = $(this);
        entry.find(".editable").attr("contenteditable", function (i, v) {
            if(v == "true") {
                entry.find(".entrySaveIcon").addClass("hide");
                $(that).removeClass("actionIconPressed").addClass("actionIcon");
            } else {
                entry.find(".entrySaveIcon").removeClass("hide");
                $(that).removeClass("actionIcon").addClass("actionIconPressed");
            }
            return !(v == "true");
        });
    });

    $(document).on("click", ".entryTag", function (e) {
        var entry = $(this).closest("entry-data");
        var tagContainer = $(".tagContainer", entry);

        if($("input.tag", tagContainer)[0]) {
            tagContainer.empty();
            $(this).removeClass("actionIconPressed").addClass("actionIcon");
            $(".entrySaveIcon", entry).addClass("hide");
        } else {
            $(this).removeClass("actionIcon").addClass("actionIconPressed");
            $(".entrySaveIcon", entry).removeClass("hide");
            $("<input type='text'/>").addClass("tag").appendTo(tagContainer);
            
            tagContainer.find(".tag").textext({
                plugins : 'autocomplete tags ajax',
                tagsItems : $("span.label", entry).toArray().map(function(d) {
                    return $(d).text();
                }),
                ajax : {
                    url : '/entry/tags',
                    dataType : 'json'
                }
            });
            $("input.tag", tagContainer).focus();
            $("input.tag", tagContainer).focusout(function() {
                $(".entrySave", entry).trigger('click');
            });
        }
    });

    /**
        save context or tags
    **/
    $(document).on("click", ".entrySave", function (e) {
        var entry = $(this).closest("entry-data");
        $(".entrySaveIcon", entry).addClass("hide");
        $(".editable", entry).prop("contenteditable", "false");
        $(".entryLoadingIcon", entry).removeClass("hide");

        $("i.actionIconPressed", entry).each(function(e) {
            $(this).removeClass("actionIconPressed").addClass("actionIcon");
            if($(this).hasClass("entryEdit")) {
                $.ajax({
                        contentType: 'application/json',
                        type: "POST",
                        url: "/entry/save",
                        dataType: 'json',
                        data: JSON.stringify({
                            id: parseInt($(entry).attr("entryId")),
                            headword: entry.find(".entry-headword").text(),
                            context: entry.find(".entry-context").html()
                        }),
                        success: function () {
                            entry.find(".entryLoadingIcon").addClass("hide");
                        },
                        error: function (jqXHR) {
                            $("#infoModalContent").text(JSON.stringify(jqXHR.responseJSON));
                            $('#infoModal').foundation('reveal', 'open');
                        }
                });
            }
            if($(this).hasClass("entryTag")) {
                var tags = eval($("div.text-wrap input:hidden", entry).val());
                $.ajax({
                        contentType: 'application/json',
                        type: "POST",
                        url: "/entry/setTags",
                        dataType: 'json',
                        data: JSON.stringify({
                            id: parseInt($(entry).attr("entryId")),
                            value: tags.join()
                        }),
                        success: function () {
                            $(".entryLoadingIcon", entry).addClass("hide");
                            $(".tagContainer", entry).empty();
                            //update tags at client side
                            var tagRow = $("div.tagRow", entry).empty();
                            tags.map(function (d){
                                    tagRow.append($("<span/>").text(d).addClass("label")).append(" ");
                                }
                            );
                        },
                        error: function (jqXHR) {
                            $("#infoModalContent").text(JSON.stringify(jqXHR.responseJSON));
                            $('#infoModal').foundation('reveal', 'open');
                        }
                });
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
