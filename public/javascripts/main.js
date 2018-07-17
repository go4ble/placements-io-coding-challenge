$(function () {
    // expand table columns when hovered over
    $('.hover-expand')
        .mouseenter(function () {
            selectColumn($(this)).addClass('in');
        }).mouseleave(function () {
            selectColumn($(this)).removeClass('in');
        });

    var selectColumn = function (el) {
        var cell = el[0].cellIndex + 1;
        return $('td.hover-expand:nth-child(' + cell + ')');
    };


    // submit new request with applied filters
    $('#apply-campaign-id-filters').click(function () {
        var url = $(this)[0].dataset['clearedRoute'];
        var values = $('#campaign-filter-select').val();
        for (var i = 0; i < values.length; i++) {
            url += "campaignId=" + values[i];
        }
        window.location.href = url;
    });


    // setup filters
    $('#campaign-filter-select').multiselect({
        enableFiltering: true,
        enableCaseInsensitiveFiltering: true,
        maxHeight: 300,
        buttonClass: 'btn btn-light',
        buttonText: function (options) { return options.length + " selected" },
        templates: {
            filterClearBtn:
                '<span class="input-group-prepend">' +
                    '<button class="btn btn-light multiselect-clear-filter" type="button">' +
                        '<i class="fas fa-backspace"></i>' +
                    '</button>' +
                '</span>'
        },
        onInitialized: function () {
            $('.campaign-filters').removeClass('d-none');
        }
    });


    // automatically submit form when file is selected
    $('#input-import').change(function () {
        $(this).parents("form").submit();
    });
});
