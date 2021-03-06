function ServiceActionsCtrl($scope, ServiceActionsService, ngTableParams, $filter) {

    $scope.btnRegenerateDisabled = false;
    $scope.info = "";

    ServiceActionsService.findAll().
        success(function (serviceActions) {
            $scope.serviceActions = serviceActions.data;
            $scope.tableParams = new ngTableParams({
                page: 1,            // show first page
                count: 10,          // count per page
                sorting: {
                    'name': 'asc'     // initial sorting
                }
            }, {
                total: $scope.serviceActions.length, // length of data
                getData: function ($defer, params) {
                    var datafilter = $filter('customAndSearch');
                    var serviceActionsData = datafilter($scope.serviceActions, $scope.tableFilter);
                    var orderedData = params.sorting() ? $filter('orderBy')(serviceActionsData, params.orderBy()) : serviceActionsData;
                    var res = orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count());
                    params.total(orderedData.length)
                    $defer.resolve(res);
                },
                $scope: { $data: {} }
            });

            $scope.$watch("tableFilter", function () {
                $scope.tableParams.reload()
            });
        })
        .error(function (resp) {
            console.log("Error with ServiceActionsService.findAll" + resp);
        });

    $scope.regenerate = function () {
        $scope.info = "Running Generation...";
        $scope.btnRegenerateDisabled = true;

        ServiceActionsService.regenerate()
            .success(function (resp) {
                $scope.info = "Success generate ServiceAction list";
                $scope.btnRegenerateDisabled = false;
            })
            .error(function (resp) {
                console.log("Error with ServiceActionsService.regenerate" + resp);
                $scope.info = "Error with generate ServiceAction list. See server logs.";
            });
    }

}

function ServiceActionEditCtrl($scope, $routeParams, $location, ServiceAction) {

    var self = this;

    ServiceAction.get({serviceActionId: $routeParams.serviceActionId}, function (serviceAction) {
        self.original = serviceAction;
        $scope.serviceAction = new ServiceAction(self.original);
    });

    $scope.isClean = function () {
        return angular.equals(self.original, $scope.serviceAction);
    };

    $scope.save = function () {
        $scope.serviceAction.update(function () {
            $location.path('/serviceactions');
        }, function (response) { // error case
            alert(response.data);
        });
    };
}
