import { Context, APIGatewayProxyResult, APIGatewayEvent } from 'aws-lambda';
import { GameLift } from 'aws-sdk';
import { getErrorMessage, isNotFoundException } from './utility-functions';

const gamelift = new GameLift();

export const handler = async (event: APIGatewayEvent, context: Context): Promise<APIGatewayProxyResult> => {
    console.log(`Event: ${JSON.stringify(event, null, 2)}`);
    console.log(`Context: ${JSON.stringify(context, null, 2)}`);

    let requestBody;
    try {
        requestBody = JSON.parse(event.body || '{}');
    } catch (err) {
        console.log(getErrorMessage(err));
        return {
            statusCode: 400,
            headers: { 'content-type': 'application/json' },
            body: JSON.stringify({
                message: 'Invalid JSON body'
            }),
        };
    }

    try {
        let params : GameLift.StartGameSessionPlacementInput = {
            PlacementId: requestBody["placementId"],
            GameSessionQueueName: requestBody["sessionQueueName"],
            MaximumPlayerSessionCount: requestBody["maximumPlayers"],
            GameProperties: requestBody["gameProperties"],
            GameSessionData: requestBody["gameSessionData"],
        }
        const startGSOut = await gamelift.startGameSessionPlacement(params).promise();
        console.log(`gamelift.startGameSession result: ${JSON.stringify(startGSOut, null, 2)}`);

        return {
            statusCode: 200,
            body: ""
        };

    } catch (err) {
        console.error(getErrorMessage(err));

        if (isNotFoundException(err)) {
            return {
                statusCode: 400,
                body: getErrorMessage(err)
            };
        }
        return {
            statusCode: 500,
            body: 'Internal error, please try again later'
        };
    }
};
