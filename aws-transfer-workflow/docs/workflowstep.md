# AWS::Transfer::Workflow WorkflowStep

The basic building block of a workflow.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#copystepdetails" title="CopyStepDetails">CopyStepDetails</a>" : <i><a href="workflowstep.md">WorkflowStep</a></i>,
    "<a href="#customstepdetails" title="CustomStepDetails">CustomStepDetails</a>" : <i><a href="workflowstep.md">WorkflowStep</a></i>,
    "<a href="#decryptstepdetails" title="DecryptStepDetails">DecryptStepDetails</a>" : <i><a href="workflowstep.md">WorkflowStep</a></i>,
    "<a href="#deletestepdetails" title="DeleteStepDetails">DeleteStepDetails</a>" : <i><a href="workflowstep.md">WorkflowStep</a></i>,
    "<a href="#tagstepdetails" title="TagStepDetails">TagStepDetails</a>" : <i><a href="workflowstep.md">WorkflowStep</a></i>,
    "<a href="#type" title="Type">Type</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#copystepdetails" title="CopyStepDetails">CopyStepDetails</a>: <i><a href="workflowstep.md">WorkflowStep</a></i>
<a href="#customstepdetails" title="CustomStepDetails">CustomStepDetails</a>: <i><a href="workflowstep.md">WorkflowStep</a></i>
<a href="#decryptstepdetails" title="DecryptStepDetails">DecryptStepDetails</a>: <i><a href="workflowstep.md">WorkflowStep</a></i>
<a href="#deletestepdetails" title="DeleteStepDetails">DeleteStepDetails</a>: <i><a href="workflowstep.md">WorkflowStep</a></i>
<a href="#tagstepdetails" title="TagStepDetails">TagStepDetails</a>: <i><a href="workflowstep.md">WorkflowStep</a></i>
<a href="#type" title="Type">Type</a>: <i>String</i>
</pre>

## Properties

#### CopyStepDetails

_Required_: No

_Type_: <a href="workflowstep.md">WorkflowStep</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CustomStepDetails

_Required_: No

_Type_: <a href="workflowstep.md">WorkflowStep</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DecryptStepDetails

_Required_: No

_Type_: <a href="workflowstep.md">WorkflowStep</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DeleteStepDetails

_Required_: No

_Type_: <a href="workflowstep.md">WorkflowStep</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TagStepDetails

_Required_: No

_Type_: <a href="workflowstep.md">WorkflowStep</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Type

_Required_: No

_Type_: String

_Allowed Values_: <code>COPY</code> | <code>CUSTOM</code> | <code>DECRYPT</code> | <code>DELETE</code> | <code>TAG</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

