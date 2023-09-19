import {Construct} from "constructs";
import * as lambdanodejs from 'aws-cdk-lib/aws-lambda-nodejs';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as cdk from 'aws-cdk-lib';
import * as iam from 'aws-cdk-lib/aws-iam';

export interface LambdaProps {
    lambdaRole: iam.IRole,
}
export class GameLiftLambda extends Construct {

    public readonly startGameSessionFunction: lambda.IFunction;
    public readonly getGameSessionPlacementFunction: lambda.IFunction;
    constructor(scope: Construct, id: string, props:LambdaProps) {
        super(scope, id);
        const functionSettings : lambdanodejs.NodejsFunctionProps = {
            handler: 'handler',
            runtime: lambda.Runtime.NODEJS_18_X,
            memorySize: 128,
            timeout: cdk.Duration.seconds(60),
            architecture: cdk.aws_lambda.Architecture.X86_64,
            role: props.lambdaRole,
            logRetention: cdk.aws_logs.RetentionDays.ONE_WEEK
        }

        this.startGameSessionFunction = new lambdanodejs.NodejsFunction(this, 'StartGameSession', {
            functionName: 'test-start-game-session',
            entry: './resource/lambda-scripts/start-game-session.ts',
            environment: {
            },
            ...functionSettings
        });

        this.getGameSessionPlacementFunction = new lambdanodejs.NodejsFunction(this, 'GetGameSessionPlacement', {
            functionName: 'test-get-game-session',
            entry: './resource/lambda-scripts/get-game-session.ts',
            environment: {
            },
            ...functionSettings
        });

    }
}