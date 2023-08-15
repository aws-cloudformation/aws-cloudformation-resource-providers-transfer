# AWS::Transfer::Server WorkflowDetails

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#onpartialupload" title="OnPartialUpload">OnPartialUpload</a>" : <i>[ <a href="workflowdetail.md">WorkflowDetail</a>, ... ]</i>,
    "<a href="#onupload" title="OnUpload">OnUpload</a>" : <i>[ <a href="workflowdetail.md">WorkflowDetail</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#onpartialupload" title="OnPartialUpload">OnPartialUpload</a>: <i>
      - <a href="workflowdetail.md">WorkflowDetail</a></i>
<a href="#onupload" title="OnUpload">OnUpload</a>: <i>
      - <a href="workflowdetail.md">WorkflowDetail</a></i>
</pre>

## Properties

#### OnPartialUpload

_Required_: No

_Type_: List of <a href="workflowdetail.md">WorkflowDetail</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### OnUpload

_Required_: No

_Type_: List of <a href="workflowdetail.md">WorkflowDetail</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

