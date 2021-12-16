import org.centos.contra.pipeline.Utils

/**
 * Defines the external call content of a metrics message
 * This will merge parameters with the defaults and will validate each parameter
 * @param parameters
 * @return HashMap
 */
def call(Map parameters = [:]) {

    def utils = new Utils()

    def defaults = readJSON text: libraryResource('msgBus-Metric-ExternalCall-Content.json')

    return { Map runtimeArgs = [:] ->
        parameters['service'] = parameters['service'] ?: msgBusMetricServiceContent()()
        parameters['error'] = parameters['error'] ?: msgBusMetricErrorContent()()
        parameters['retryData'] = parameters['retryData'] ?: msgBusMetricRetryDataContent()()

        parameters = utils.mapMergeQuotes([parameters, runtimeArgs])
        try {
            mergedMessage = utils.mergeBusMessage(parameters, defaults)
        } catch(e) {
            throw new Exception("Creating closure for external call content failed: " + e)
        }

        // sendCIMessage expects String arguments
        return utils.getMapStringColon(mergedMessage)
    }
}
