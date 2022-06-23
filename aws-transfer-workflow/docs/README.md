# AWS::Transfer::Workflow

Resource Type definition for AWS::Transfer::Workflow

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Transfer::Workflow",
    "Properties" : {
        "<a href="#onexceptionsteps" title="OnExceptionSteps">OnExceptionSteps</a>" : <i>[ <a href="workflowstep.md">WorkflowStep</a>, ... ]</i>,
        "<a href="#steps" title="Steps">Steps</a>" : <i>[ <a href="workflowstep.md">WorkflowStep</a>, ... ]</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#description" title="Description">Description</a>" : <i>String</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::Transfer::Workflow
Properties:
    <a href="#onexceptionsteps" title="OnExceptionSteps">OnExceptionSteps</a>: <i>
      - <a href="workflowstep.md">WorkflowStep</a></i>
    <a href="#steps" title="Steps">Steps</a>: <i>
      - <a href="workflowstep.md">WorkflowStep</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#description" title="Description">Description</a>: <i>String</i>
</pre>

## Properties

#### OnExceptionSteps

Specifies the steps (actions) to take if any errors are encountered during execution of the workflow.

_Required_: No

_Type_: List of <a href="workflowstep.md">WorkflowStep</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Steps

Specifies the details for the steps that are in the specified workflow.

_Required_: Yes

_Type_: List of <a href="workflowstep.md">WorkflowStep</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Tags

Key-value pairs that can be used to group and search for workflows. Tags are metadata attached to workflows for any purpose.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Description

A textual description for the workflow.

_Required_: No

_Type_: String

_Maximum_: <code>256</code>

_Pattern_: <code>^[\w\- ]*$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the WorkflowId.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### WorkflowId

A unique identifier for the workflow.

#### Arn

Specifies the unique Amazon Resource Name (ARN) for the workflow.

