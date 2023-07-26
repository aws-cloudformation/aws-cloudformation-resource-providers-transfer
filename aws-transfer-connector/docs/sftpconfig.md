# AWS::Transfer::Connector SftpConfig

Configuration for an SFTP connector.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#usersecretid" title="UserSecretId">UserSecretId</a>" : <i>String</i>,
    "<a href="#trustedhostkeys" title="TrustedHostKeys">TrustedHostKeys</a>" : <i>[ String, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#usersecretid" title="UserSecretId">UserSecretId</a>: <i>String</i>
<a href="#trustedhostkeys" title="TrustedHostKeys">TrustedHostKeys</a>: <i>
      - String</i>
</pre>

## Properties

#### UserSecretId

ARN or name of the secret in AWS Secrets Manager which contains the SFTP user's private keys or passwords.

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>2048</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TrustedHostKeys

List of public host keys, for the external server to which you are connecting.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

