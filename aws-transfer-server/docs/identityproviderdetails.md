# AWS::Transfer::Server IdentityProviderDetails

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#directoryid" title="DirectoryId">DirectoryId</a>" : <i>String</i>,
    "<a href="#function" title="Function">Function</a>" : <i>String</i>,
    "<a href="#invocationrole" title="InvocationRole">InvocationRole</a>" : <i>String</i>,
    "<a href="#sftpauthenticationmethods" title="SftpAuthenticationMethods">SftpAuthenticationMethods</a>" : <i>String</i>,
    "<a href="#url" title="Url">Url</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#directoryid" title="DirectoryId">DirectoryId</a>: <i>String</i>
<a href="#function" title="Function">Function</a>: <i>String</i>
<a href="#invocationrole" title="InvocationRole">InvocationRole</a>: <i>String</i>
<a href="#sftpauthenticationmethods" title="SftpAuthenticationMethods">SftpAuthenticationMethods</a>: <i>String</i>
<a href="#url" title="Url">Url</a>: <i>String</i>
</pre>

## Properties

#### DirectoryId

_Required_: No

_Type_: String

_Minimum Length_: <code>12</code>

_Maximum Length_: <code>12</code>

_Pattern_: <code>^d-[0-9a-f]{10}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Function

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>170</code>

_Pattern_: <code>^arn:[a-z-]+:lambda:.*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### InvocationRole

_Required_: No

_Type_: String

_Minimum Length_: <code>20</code>

_Maximum Length_: <code>2048</code>

_Pattern_: <code>^arn:.*role/</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SftpAuthenticationMethods

_Required_: No

_Type_: String

_Allowed Values_: <code>PASSWORD</code> | <code>PUBLIC_KEY</code> | <code>PUBLIC_KEY_OR_PASSWORD</code> | <code>PUBLIC_KEY_AND_PASSWORD</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Url

_Required_: No

_Type_: String

_Maximum Length_: <code>255</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

