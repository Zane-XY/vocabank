import org.joda.time.DateTime

class Entry (
    name : String,
    source : List[ String ],
    content : String,
    added : DateTime,
    updated : DateTime,
    tags : List[String],
    rating : Int
)
