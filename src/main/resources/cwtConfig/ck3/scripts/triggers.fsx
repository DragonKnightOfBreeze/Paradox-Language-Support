open System.Text
// #r @"C:\Users\Thomas\.nuget\packages\dotnet.glob\2.0.3\lib\netstandard1.1\DotNet.Glob.dll"
// #r @"C:\Users\Thomas\.nuget\packages\fparsec\1.0.4-rc3\lib\netstandard1.6\FParsec.dll"
// #r @"C:\Users\Thomas\.nuget\packages\fparsec\1.0.4-rc3\lib\netstandard1.6\FParsecCS.dll"
// #r @"C:\Users\Thomas\.nuget\packages\fsharp.collections.parallelseq\1.1.2\lib\netstandard2.0\FSharp.Collections.ParallelSeq.dll"
// #r @"C:\Users\Thomas\.nuget\packages\fsharp.data\3.0.0-beta\lib\netstandard2.0\FSharp.Data.dll"
// #r @"C:\Users\Thomas\.nuget\packages\fsharpx.collections\1.17.0\lib\net40\FSharpx.Collections.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\Microsoft.Win32.Primitives.dll"
// // #r @"C:\Users\Thomas\.nuget\packages\newtonsoft.json.fsharp\3.2.2\lib\net40\Newtonsoft.Json.FSharp.dll"
// // #r @"C:\Users\Thomas\.nuget\packages\newtonsoft.json\11.0.2\lib\netstandard2.0\Newtonsoft.Json.dll"
// // #r @"C:\Users\Thomas\.nuget\packages\nodatime\1.4.5\lib\net35-Client\NodaTime.dll"
// #r @"C:\Users\Thomas\.nuget\packages\sandwych.quickgraph.core\1.0.0\lib\netstandard2.0\Sandwych.QuickGraph.Core.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.AppContext.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Collections.Concurrent.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Collections.NonGeneric.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Collections.Specialized.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Collections.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.ComponentModel.Composition.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.ComponentModel.EventBasedAsync.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.ComponentModel.Primitives.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.ComponentModel.TypeConverter.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.ComponentModel.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Console.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Core.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Data.Common.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Data.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Diagnostics.Contracts.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Diagnostics.Debug.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Diagnostics.FileVersionInfo.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Diagnostics.Process.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Diagnostics.StackTrace.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Diagnostics.TextWriterTraceListener.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Diagnostics.Tools.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Diagnostics.TraceSource.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Diagnostics.Tracing.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Drawing.Primitives.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Drawing.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Dynamic.Runtime.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Globalization.Calendars.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Globalization.Extensions.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Globalization.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.IO.Compression.FileSystem.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.IO.Compression.ZipFile.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.IO.Compression.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.IO.FileSystem.DriveInfo.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.IO.FileSystem.Primitives.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.IO.FileSystem.Watcher.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.IO.FileSystem.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.IO.IsolatedStorage.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.IO.MemoryMappedFiles.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.IO.Pipes.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.IO.UnmanagedMemoryStream.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.IO.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Linq.Expressions.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Linq.Parallel.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Linq.Queryable.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Linq.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Net.Http.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Net.NameResolution.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Net.NetworkInformation.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Net.Ping.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Net.Primitives.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Net.Requests.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Net.Security.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Net.Sockets.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Net.WebHeaderCollection.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Net.WebSockets.Client.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Net.WebSockets.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Net.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Numerics.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.ObjectModel.dll"
// #r @"C:\Users\Thomas\.nuget\packages\system.reflection.emit.ilgeneration\4.3.0\ref\netstandard1.0\System.Reflection.Emit.ILGeneration.dll"
// #r @"C:\Users\Thomas\.nuget\packages\system.reflection.emit.lightweight\4.3.0\ref\netstandard1.0\System.Reflection.Emit.Lightweight.dll"
// #r @"C:\Users\Thomas\.nuget\packages\system.reflection.emit\4.3.0\ref\netstandard1.1\System.Reflection.Emit.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Reflection.Extensions.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Reflection.Primitives.dll"
// #r @"C:\Users\Thomas\.nuget\packages\system.reflection.typeextensions\4.5.0\ref\netstandard2.0\System.Reflection.TypeExtensions.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Reflection.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Resources.Reader.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Resources.ResourceManager.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Resources.Writer.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Runtime.CompilerServices.VisualC.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Runtime.Extensions.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Runtime.Handles.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Runtime.InteropServices.RuntimeInformation.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Runtime.InteropServices.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Runtime.Numerics.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Runtime.Serialization.Formatters.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Runtime.Serialization.Json.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Runtime.Serialization.Primitives.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Runtime.Serialization.Xml.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Runtime.Serialization.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Runtime.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Security.Claims.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Security.Cryptography.Algorithms.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Security.Cryptography.Csp.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Security.Cryptography.Encoding.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Security.Cryptography.Primitives.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Security.Cryptography.X509Certificates.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Security.Principal.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Security.SecureString.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.ServiceModel.Web.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Text.Encoding.Extensions.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Text.Encoding.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Text.RegularExpressions.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Threading.Overlapped.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Threading.Tasks.Parallel.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Threading.Tasks.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Threading.Thread.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Threading.ThreadPool.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Threading.Timer.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Threading.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Transactions.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.ValueTuple.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Web.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Windows.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Xml.Linq.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Xml.ReaderWriter.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Xml.Serialization.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Xml.XDocument.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Xml.XPath.XDocument.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Xml.XPath.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Xml.XmlDocument.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Xml.XmlSerializer.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.Xml.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\System.dll"
// #r @"C:\Users\Thomas\.nuget\packages\netstandard.library\2.0.3\build\netstandard2.0\ref\netstandard.dll"
#r "nuget: CWTools"
#r @"C:\Users\Thomas\git\cwtools/CWTools/bin/Debug/netstandard2.0/CWTools.dll"
open System

open System.IO
open System.Security.Cryptography

open CWTools.Common
open CWTools.Parser
open FParsec
Encoding.RegisterProvider(CodePagesEncodingProvider.Instance);

eprintfn "%A" (JominiParser.parseTriggerFile @"..\script-docs\triggers.log")
let triggers = JominiParser.parseTriggerFilesRes @"..\script-docs\triggers.log"
let effects = JominiParser.parseEffectFilesRes @"..\script-docs\effects.log"

type Trait =
| Comparison
| Bool
| Date
| Omen
| CharacterScope
| CharacterTarget
| LandedTitleTarget
| LandedTitleScope
| CultureGroupScope
| FaithScope
| ReligionScope
| ProvinceTarget
| Province
| Country
| Religion
| Culture
| Diplo
| Subject

let traitParse (x : string) =
    match x with
        | "<, <=, =, !=, >, >="-> Comparison
        | "yes/no"-> Bool
        | "<, =, > valid date"-> Date
        | "class COmenDataBase key"-> Omen
        | "character scope"-> CharacterScope
        | "character target"-> CharacterTarget
        | "landed title target"-> LandedTitleTarget
        | "landed title scope"-> LandedTitleScope
        | "culture group scope"-> CultureGroupScope
        | "faith scope"-> FaithScope
        | "religion scope"-> ReligionScope
        | "province target"-> ProvinceTarget
        | "province id/province scope"-> Province
        | "country tag/country scope"-> Country
        | "class CReligionDatabase key/religion scope"-> Religion
        | "culture db key/culture scope"-> Culture
        | "class CDiplomaticStanceDatabase key"-> Diplo
        | "class CSubjectTypeDatabase key"-> Subject
        | x -> (eprintfn "%A" x); Subject

let traitToRHS (x : Trait) =
    match x with
        | Comparison -> "==", ["replace_me_comparison"]
        | Bool -> "=", ["replace_me_bool"]
        | Date -> "==", ["replace_me_date"]
        | Omen -> "=", ["replace_me_omen"]
        | CharacterScope -> "=", ["replace_me_character"]
        | CharacterTarget -> "=", ["replace_me_character_target"]
        | LandedTitleTarget -> "=", ["replace_me_landed_title"]
        | CultureGroupScope -> "=", ["replace_me_culture_group"]
        | FaithScope -> "=", ["replace_me_faith"]
        | ReligionScope -> "=", ["replace_me_religion"]
        | ProvinceTarget -> "=", ["replace_me_province_target"]
        | LandedTitleScope -> "=", ["replace_me_landed_title_scope"]
        | ReligionScope -> "=", ["replace_me_religion"]
        | Province -> "=", ["replace_me_province_id"; "replace_me_province_scope"]
        | Country -> "=", ["replace_me_country_tag"; "replace_me_country_scope"]
        | Religion -> "=", ["replace_me_religion_tag"; "replace_me_religion_scope"]
        | Culture -> "=", ["replace_me_culture_tag"; "replace_me_culture_scope"]
        | Diplo -> "=", ["replace_me_diplo"]
        | Subject -> "=", ["replace_me_subject_type"]

let tscope = true

let anytemplate =
        """{
    ## cardinality = 0..1
    percent = value_float[0.0..1.0]
    ## cardinality = 0..1
    count = int_value_field
    ## cardinality = 0..1
    count = all
    alias_name[trigger] = alias_match_left[trigger]
}"""

let everytemplate =
        """{
    ## cardinality = 0..1
    limit = {
        alias_name[trigger] = alias_match_left[trigger]
    }
    ## cardinality = 0..inf
    alternative_limit = {
        alias_name[trigger] = alias_match_left[trigger]
    }
    alias_name[effect] = alias_match_left[effect]
}"""

let randomtemplate =
        """{
    ## cardinality = 0..1
    limit = {
        alias_name[trigger] = alias_match_left[trigger]
    }
    ## cardinality = 0..inf
    alternative_limit = {
        alias_name[trigger] = alias_match_left[trigger]
    }
    ## cardinality = 0..1
    weight = single_alias_right[weight_block]
    alias_name[effect] = alias_match_left[effect]
}"""

let orderedtemplate =
        """{
    ## cardinality = 0..1
    limit = {
        alias_name[trigger] = alias_match_left[trigger]
    }
    ## cardinality = 0..1
    # TODO: Work out what exactly this is restricted to
    order_by = value_field
    ## cardinality = 0..1
    max = int_value_field
    ## cardinality = 0..1
    max = int_value_field
    ## cardinality = 0..1
    position = int
    ## cardinality = 0..1
    check_range_bounds = no
    alias_name[effect] = alias_match_left[effect]
}"""
let tinner =
            """{
	alias_name[trigger] = alias_match_left[trigger]
}
"""
let anytriggers = triggers |> List.filter (fun (t : RawEffect) -> t.name.StartsWith("any_"))
let othertriggers = triggers |> List.filter (fun (t : RawEffect) -> t.name.StartsWith("any_") |> not)
let tout =  (fun (t : RawEffect) ->
                        let scopes =
                            match t.scopes with
                            | [] -> ""
                            | [x] -> "## scopes = " + x + "\n"
                            | xs ->
                                let scopes = xs |> List.map (fun s -> s.ToString()) |> String.concat " "
                                "## scopes = { " + scopes + " }\n"
                        let scopes = if tscope then scopes else ""
                        let any = t.name.StartsWith("any_")
                        let traitEq, traitRHSs =  t.traits |> Option.map (traitParse >> traitToRHS) |> Option.defaultValue ("=", ["replace_me"])
                        let rhs =
                            if any
                            then [anytemplate]
                            else traitRHSs
                        let desc = t.desc.Replace("\n", " ")
                        // sprintf "###%s\n%salias[trigger:%s] = %s\n\r" desc scopes t.name rhs)
                        rhs |> List.map (fun rhs -> sprintf "### %s\nalias[trigger:%s] %s %s\n\r" desc t.name traitEq rhs))
                // |> String.concat("")

let filterfun (s : string) = if s.StartsWith "every_" || s.StartsWith "random_" || s.StartsWith "ordered_" then true else false

let itereffects = effects |> List.filter (fun (e : RawEffect) -> filterfun e.name)
let othereffects = effects |> List.filter (fun (e : RawEffect) -> filterfun e.name |> not)
let efun = (fun (t : RawEffect) ->
        let scopes =
            match t.scopes with
            | [] -> ""
            | [x] -> "## scopes = " + x + "\n"
            | xs ->
                let scopes = xs |> List.map (fun s -> s.ToString()) |> String.concat " "
                "## scopes = { " + scopes + " }\n"
        let scopes = if tscope then scopes else ""
        let rhs =
            match t.name with
            | x when x.StartsWith "every_" -> everytemplate
            | x when x.StartsWith "random_" -> randomtemplate
            | x when x.StartsWith "ordered_" -> orderedtemplate
            | _ -> "replace_me"
        let desc = t.desc.Replace("\n", " ")
        // sprintf "###%s\n%salias[effect:%s] = %s\n\r" desc scopes t.name rhs)
        sprintf "### %s\nalias[effect:%s] = %s\n\r" desc t.name rhs)
                // |> String.concat("")

let rulesFiles =
    [
        @"../config/triggers.cwt"
        @"../config/list_triggers.cwt"
        @"../config/effects.cwt"
        @"../config/list_effects.cwt"
    ] |> List.map (fun fn -> fn, (System.IO.File.ReadAllText fn))

open CWTools.Rules
open CWTools.Utilities

let rules, types, enums, complexenums, values =
            rulesFiles
                |> CWTools.Rules.RulesParser.parseConfigs (scopeManager.ParseScope()) (scopeManager.AllScopes) (scopeManager.AnyScope) Map.empty
let oldTriggers = rules |> List.choose (function
                                        |AliasRule ("trigger", (LeafRule(SpecificField (SpecificValue(x)),_), _)) -> Some (StringResource.stringManager.GetStringForIDs x)
                                        |AliasRule ("trigger", (NodeRule(SpecificField (SpecificValue(x)),_), _)) -> Some (StringResource.stringManager.GetStringForIDs x)
                                        |_ -> None)
let oldEffects = rules |> List.choose (function
                                        |AliasRule ("effect", (LeafRule(SpecificField (SpecificValue(x)),_), _)) -> Some (StringResource.stringManager.GetStringForIDs x)
                                        |AliasRule ("effect", (NodeRule(SpecificField (SpecificValue(x)),_), _)) -> Some (StringResource.stringManager.GetStringForIDs x)
                                        |_ -> None)

let atout = anytriggers |>  List.filter (fun e -> oldTriggers |> List.contains e.name |> not) |> List.collect tout |> String.concat("")
let otout = othertriggers |>  List.filter (fun e -> oldTriggers |> List.contains e.name |> not) |> List.collect tout |> String.concat("")

oldTriggers |> List.filter (fun e -> anytriggers |> List.exists (fun t -> t.name = e) |> not)
            |> List.filter (fun e -> othertriggers |> List.exists (fun t -> t.name = e) |> not)
            |> List.iter (fun e -> eprintfn "removed: %s" e)

let ieout = itereffects |>  List.filter (fun e -> oldEffects |> List.contains e.name |> not) |> List.map efun |> String.concat("")
let oeout = othereffects |>  List.filter (fun e -> oldEffects |> List.contains e.name |> not) |> List.map efun |> String.concat("")

oldEffects |> List.filter (fun e -> itereffects |> List.exists (fun t -> t.name = e) |> not)
            |> List.filter (fun e -> othereffects |> List.exists (fun t -> t.name = e) |> not)
            |> List.iter (fun e -> eprintfn "removed: %s" e)

File.AppendAllText("../config/triggers.cwt", otout)
File.AppendAllText("../config/list_triggers.cwt", atout)
File.AppendAllText("../config/effects.cwt", oeout)
File.AppendAllText("../config/list_effects.cwt", ieout)

// File.WriteAllText("test.test", triggers |> List.choose (fun t -> t.traits) |> List.distinct |> String.concat("\n"))