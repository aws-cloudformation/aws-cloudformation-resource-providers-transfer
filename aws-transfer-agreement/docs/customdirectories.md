# AWS::Transfer::Agreement CustomDirectories

Specifies a separate directory for each type of file to store for an AS2 message.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#failedfilesdirectory" title="FailedFilesDirectory">FailedFilesDirectory</a>" : <i>String</i>,
    "<a href="#mdnfilesdirectory" title="MdnFilesDirectory">MdnFilesDirectory</a>" : <i>String</i>,
    "<a href="#payloadfilesdirectory" title="PayloadFilesDirectory">PayloadFilesDirectory</a>" : <i>String</i>,
    "<a href="#statusfilesdirectory" title="StatusFilesDirectory">StatusFilesDirectory</a>" : <i>String</i>,
    "<a href="#temporaryfilesdirectory" title="TemporaryFilesDirectory">TemporaryFilesDirectory</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#failedfilesdirectory" title="FailedFilesDirectory">FailedFilesDirectory</a>: <i>String</i>
<a href="#mdnfilesdirectory" title="MdnFilesDirectory">MdnFilesDirectory</a>: <i>String</i>
<a href="#payloadfilesdirectory" title="PayloadFilesDirectory">PayloadFilesDirectory</a>: <i>String</i>
<a href="#statusfilesdirectory" title="StatusFilesDirectory">StatusFilesDirectory</a>: <i>String</i>
<a href="#temporaryfilesdirectory" title="TemporaryFilesDirectory">TemporaryFilesDirectory</a>: <i>String</i>
</pre>

## Properties

#### FailedFilesDirectory

Specifies a location to store the failed files for an AS2 message.

_Required_: Yes

_Type_: String

_Pattern_: <code>(|/.*)</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MdnFilesDirectory

Specifies a location to store the MDN file for an AS2 message.

_Required_: Yes

_Type_: String

_Pattern_: <code>(|/.*)</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PayloadFilesDirectory

Specifies a location to store the payload file for an AS2 message.

_Required_: Yes

_Type_: String

_Pattern_: <code>(|/.*)</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### StatusFilesDirectory

Specifies a location to store the status file for an AS2 message.

_Required_: Yes

_Type_: String

_Pattern_: <code>(|/.*)</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TemporaryFilesDirectory

Specifies a location to store the temporary processing file for an AS2 message.

_Required_: Yes

_Type_: String

_Pattern_: <code>(|/.*)</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

