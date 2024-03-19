# AWS::Transfer::Server S3StorageOptions

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#directorylistingoptimization" title="DirectoryListingOptimization">DirectoryListingOptimization</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#directorylistingoptimization" title="DirectoryListingOptimization">DirectoryListingOptimization</a>: <i>String</i>
</pre>

## Properties

#### DirectoryListingOptimization

Indicates whether optimization to directory listing on S3 servers is used. Disabled by default for compatibility.

_Required_: No

_Type_: String

_Allowed Values_: <code>ENABLED</code> | <code>DISABLED</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

