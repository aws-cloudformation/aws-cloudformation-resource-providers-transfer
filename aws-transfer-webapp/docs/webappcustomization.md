# AWS::Transfer::WebApp WebAppCustomization

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#title" title="Title">Title</a>" : <i>String</i>,
    "<a href="#logofile" title="LogoFile">LogoFile</a>" : <i>String</i>,
    "<a href="#faviconfile" title="FaviconFile">FaviconFile</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#title" title="Title">Title</a>: <i>String</i>
<a href="#logofile" title="LogoFile">LogoFile</a>: <i>String</i>
<a href="#faviconfile" title="FaviconFile">FaviconFile</a>: <i>String</i>
</pre>

## Properties

#### Title

Specifies a title to display on the web app.

_Required_: No

_Type_: String

_Maximum Length_: <code>100</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LogoFile

Specifies a logo to display on the web app.

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>51200</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### FaviconFile

Specifies a favicon to display in the browser tab.

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>20960</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

