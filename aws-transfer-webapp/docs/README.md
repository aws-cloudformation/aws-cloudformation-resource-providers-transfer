# AWS::Transfer::WebApp

Resource Type definition for AWS::Transfer::WebApp

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Transfer::WebApp",
    "Properties" : {
        "<a href="#identityproviderdetails" title="IdentityProviderDetails">IdentityProviderDetails</a>" : <i><a href="identityproviderdetails.md">IdentityProviderDetails</a></i>,
        "<a href="#accessendpoint" title="AccessEndpoint">AccessEndpoint</a>" : <i>String</i>,
        "<a href="#webappunits" title="WebAppUnits">WebAppUnits</a>" : <i><a href="webappunits.md">WebAppUnits</a></i>,
        "<a href="#webappcustomization" title="WebAppCustomization">WebAppCustomization</a>" : <i><a href="webappcustomization.md">WebAppCustomization</a></i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Transfer::WebApp
Properties:
    <a href="#identityproviderdetails" title="IdentityProviderDetails">IdentityProviderDetails</a>: <i><a href="identityproviderdetails.md">IdentityProviderDetails</a></i>
    <a href="#accessendpoint" title="AccessEndpoint">AccessEndpoint</a>: <i>String</i>
    <a href="#webappunits" title="WebAppUnits">WebAppUnits</a>: <i><a href="webappunits.md">WebAppUnits</a></i>
    <a href="#webappcustomization" title="WebAppCustomization">WebAppCustomization</a>: <i><a href="webappcustomization.md">WebAppCustomization</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### IdentityProviderDetails

You can provide a structure that contains the details for the identity provider to use with your web app.

_Required_: Yes

_Type_: <a href="identityproviderdetails.md">IdentityProviderDetails</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AccessEndpoint

The AccessEndpoint is the URL that you provide to your users for them to interact with the Transfer Family web app. You can specify a custom URL or use the default value.

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>1024</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### WebAppUnits

A union that contains the value for number of concurrent connections or the user sessions on your web app.

_Required_: No

_Type_: <a href="webappunits.md">WebAppUnits</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### WebAppCustomization

_Required_: No

_Type_: <a href="webappcustomization.md">WebAppCustomization</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

Key-value pairs that can be used to group and search for web apps.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Arn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Arn

Specifies the unique Amazon Resource Name (ARN) for the web app.

#### WebAppId

A unique identifier for the web app.

#### ApplicationArn

Returns the <code>ApplicationArn</code> value.

