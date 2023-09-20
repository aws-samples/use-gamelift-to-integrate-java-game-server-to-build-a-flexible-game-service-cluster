import { Context, APIGatewayProxyResult, APIGatewayEvent } from 'aws-lambda';
import { GameLift } from 'aws-sdk';
import { getErrorMessage, isNotFoundException } from './utility-functions';

const gamelift = new GameLift();

export const handler = async (event: APIGatewayEvent, context: Context): Promise<APIGatewayProxyResult> => {
    console.log(`Event: ${JSON.stringify(event, null, 2)}`);
    console.log(`Context: ${JSON.stringify(context, null, 2)}`);

    if (event.pathParameters == null || event.pathParameters['placement-id'] == null) {
        return {
            statusCode: 400,
            headers: { 'content-type': 'application/json' },
            body: JSON.stringify({
                message: 'No placement-id in request path'
            })
        }
    }

    const placementId = event.pathParameters['placement-id'] as string;
    console.log(`placement id: ${placementId}`);

    try {
        let params : GameLift.DescribeGameSessionPlacementInput = {
            PlacementId: placementId,
        }
        const getGameSessionPlacementOut = await gamelift.describeGameSessionPlacement(params).promise();
        console.log(`gamelift.get game session placement result: ${JSON.stringify(getGameSessionPlacementOut, null, 2)}`);

        return {
            statusCode: 200,
            body: JSON.stringify(getGameSessionPlacementOut, null, 2)
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
