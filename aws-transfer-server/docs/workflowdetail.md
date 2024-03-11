# AWS::Transfer::Server WorkflowDetail

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#workflowid" title="WorkflowId">WorkflowId</a>" : <i>String</i>,
    "<a href="#executionrole" title="ExecutionRole">ExecutionRole</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#workflowid" title="WorkflowId">WorkflowId</a>: <i>String</i>
<a href="#executionrole" title="ExecutionRole">ExecutionRole</a>: <i>String</i>
</pre>

## Properties

#### WorkflowId

_Required_: Yes

_Type_: String

_Minimum Length_: <code>19</code>

_Maximum Length_: <code>19</code>

_Pattern_: <code>^w-([a-z0-9]{17})$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ExecutionRole

_Required_: Yes

_Type_: String

_Minimum Length_: <code>20</code>

_Maximum Length_: <code>2048</code>

_Pattern_: <code>^arn:.*role/\S+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

