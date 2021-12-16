import org.centos.contra.pipeline.Utils

/**
 * Defines the metrics message
 * This will merge parameters with the defaults and will validate each parameter
 * @param parameters
 * @return HashMap
 */
def call(Map parameters = [:]) {

    def utils = new Utils()

    def defaults = readJSON text: libraryResource('msgBusMetricMsg.json')

    return { Map runtimeArgs = [:] ->
        parameters['externalCall'] = parameters['externalCall'] ?: msgBusMetricExternalCallContent()()
        parameters['pipeline'] = parameters['pipeline'] ?: msgBusMetricPipelineContent()()

        parameters = utils.mapMergeQuotes([parameters, runtimeArgs])
        try {
            mergedMessage = utils.mergeBusMessage(parameters, defaults)
        } catch(e) {
            throw new Exception("Creating the metrics message failed: " + e)
        }

        // sendCIMessage expects String arguments
        return utils.getMapStringColon(mergedMessage)
    }
}
