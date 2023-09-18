import {Construct} from "constructs";
import * as gamelift from 'aws-cdk-lib/aws-gamelift';
import * as cdk from 'aws-cdk-lib';

export class GameLiftService extends Construct {

    constructor(scope: Construct, id: string) {
        super(scope, id);

        let routingStrategy : gamelift.CfnAlias.RoutingStrategyProperty;
        routingStrategy = {
            type: 'TERMINAL',
            message: 'Placeholder'
        };
        const fleetAlias = new gamelift.CfnAlias(scope, 'DefaultFleetAlias', {
            name: 'java-game-server-fleet-Alias',
            routingStrategy: routingStrategy,
            description: 'Update this alias to point to a real Fleet'
        });

        const gameSessionQueue = new gamelift.CfnGameSessionQueue(scope, 'GameSessionQueue', {
            name: 'java-game-server-session-queue',
            destinations: [
                {
                    destinationArn: `arn:aws:gamelift:${cdk.Stack.of(this).region}::alias/${fleetAlias.attrAliasId}`
                }
            ],
            filterConfiguration: {
                allowedLocations: [
                    cdk.Stack.of(this).region
                ]
            },
            timeoutInSeconds: 600
        });
        gameSessionQueue.node.addDependency(fleetAlias);
    }

}